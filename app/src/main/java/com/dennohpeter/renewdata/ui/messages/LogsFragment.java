package com.dennohpeter.renewdata.ui.messages;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dennohpeter.renewdata.DatabaseHelper;
import com.dennohpeter.renewdata.MessageModel;
import com.dennohpeter.renewdata.R;
import com.dennohpeter.renewdata.Utils;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class LogsFragment extends androidx.fragment.app.Fragment {
    private static RecyclerView recyclerView;
    private static DatabaseHelper databaseHelper;
    private static LogsAdapter logsAdapter;
    private static String format_style;
    private static boolean in24hrsFormat;
    private final String SMS_PERMISSION = Manifest.permission.READ_SMS;
    private int SMS_PERMISSION_CODE = 12;
    private ProgressDialog progressDialog;
    private SQLiteDatabase db;
    private MenuItem refreshMessages;
    private TextView nothing_to_show;
    private Context context;
    private SwipeRefreshLayout refreshLayout;
    private ProgressBar progressBar;
    private Utils utils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.context = getContext();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_logs, container, false);
        this.context = getContext();
        this.utils = new Utils();
        // Bind views
        refreshLayout = root.findViewById(R.id.refreshLayout);
        setSwipeRefreshView();

        recyclerView = root.findViewById(R.id.logs_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);

        nothing_to_show = root.findViewById(R.id.nothing_to_show);
        progressBar = root.findViewById(R.id.progress_circular);
        // get preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        in24hrsFormat = preferences.getBoolean("twenty4_hour_clock", false);
        format_style = preferences.getString("date_format", getString(R.string.default_date_format));

        logsAdapter = new LogsAdapter();
        recyclerView.setAdapter(logsAdapter);
        databaseHelper = new DatabaseHelper(context);
        new populateRecyclerView().execute();
        return root;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        refreshMessages = menu.add(context.getString(R.string.refreshMessages));
        refreshMessages.setIcon(R.drawable.ic_refresh);
        refreshMessages.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item == refreshMessages) {
            refreshLayout.setRefreshing(true);
            // refresh messages
            LogsFragmentPermissionsDispatcher.refreshMessagesWithPermissionCheck(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NeedsPermission(SMS_PERMISSION)
    void refreshMessages() {
        new SyncMessages().execute();
    }

    @OnShowRationale(SMS_PERMISSION)
    void showRationaleForSMS(PermissionRequest request) {
        utils.showRationaleDialog(context, getString(R.string.permission_sms_rationale, getString(R.string.app_name)), request);
    }

    @OnPermissionDenied(SMS_PERMISSION)
    void onSMSDenied() {
        Toast.makeText(context, R.string.permission_sms_denied, Toast.LENGTH_SHORT).show();
        refreshLayout.setRefreshing(false);
    }

    @OnNeverAskAgain(SMS_PERMISSION)
    void onSMSNeverAskAgain() {
        Toast.makeText(context, R.string.permission_sms_never_ask_again, Toast.LENGTH_SHORT).show();
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void setSwipeRefreshView() {
        // the refreshing colors
        refreshLayout.setColorSchemeColors(getResources().
                        getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light)
                , getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light));

        refreshLayout.setOnRefreshListener(() -> {
            LogsFragmentPermissionsDispatcher.refreshMessagesWithPermissionCheck(this);
        });
    }

    private class populateRecyclerView extends AsyncTask<Void, MessageModel, Void> {
        Cursor cursor;
        SQLiteDatabase db;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... voids) {

            db = databaseHelper.getReadableDatabase();
            cursor = databaseHelper.getLogMessages(db);
            while (cursor.moveToNext()) {
                String msg_from = cursor.getString(cursor.getColumnIndex("msg_from"));
                String msg_body = cursor.getString(cursor.getColumnIndex("msg_body"));
                long dateInMilliseconds = cursor.getLong(cursor.getColumnIndex("received_date"));
                String formatted_date = new Utils().formatDate(dateInMilliseconds, format_style, in24hrsFormat, "\n");
                publishProgress(new MessageModel(formatted_date, msg_body, msg_from));
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(MessageModel... message) {
            super.onProgressUpdate(message);
            logsAdapter.add(message[0]);
        }

        @Override
        protected void onPostExecute(Void s) {
            cursor.close();
            db.close();
            if (logsAdapter.size() == 0) {
                recyclerView.setVisibility(View.GONE);
                nothing_to_show.setVisibility(View.VISIBLE);
            } else {
                nothing_to_show.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            recyclerView.setAdapter(logsAdapter);
            progressBar.setVisibility(View.GONE);

        }
    }


    class SyncMessages extends AsyncTask<Void, Integer, String> {
        Cursor cursor;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage(getString(R.string.syncing_with_messages));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... strings) {
            cursor = context.getContentResolver().query(Telephony.Sms.CONTENT_URI, null, null, null, null);
            if (cursor != null) {
                db = databaseHelper.getWritableDatabase();
                int count = 0;
                while (cursor.moveToNext()) {
                    String msg_from = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                    String msg_body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                    long msg_date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                    // filtering telkom messages
                    if (msg_from.toLowerCase().contains(context.getString(R.string.telkom).toLowerCase())) {
                        // check if message body contains the following keywords
                        // have recharged, have exhausted,have subscribed, balance, awarded, received
                        if (msg_body.contains("recharged") | msg_body.contains("exhausted") | msg_body.contains("subscribed") | msg_body.contains("balance") | msg_body.contains("awarded") | msg_body.contains("received")) {
                            // updating or creating the logs
                            databaseHelper.create_or_update_logs(msg_from, msg_body, msg_date);
                        }
                    }
                    // TODO add more sms filters e.g SAF, AIRTEL.

                    publishProgress(count * 100 / cursor.getCount());
                    count++;
                }
                return "success";
            }
            return "failed";

        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("success")) {
                db.close();
                progressDialog.dismiss();
                refreshLayout.setRefreshing(false);
                // after successful fetch populate recycler viewer
                new populateRecyclerView().execute();
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "No Messages", Toast.LENGTH_LONG).show();
                }
            }
            cursor.close();
        }
    }

}
