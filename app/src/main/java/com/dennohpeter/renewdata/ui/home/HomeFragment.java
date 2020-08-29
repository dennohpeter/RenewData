package com.dennohpeter.renewdata.ui.home;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.dennohpeter.renewdata.BroadcastManager;
import com.dennohpeter.renewdata.R;
import com.dennohpeter.renewdata.TimeManager;
import com.dennohpeter.renewdata.Utils;

import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends androidx.fragment.app.Fragment {
    private IntentFilter filter;
    private TextView purchased_tmView, expiry_tmView, tm_leftView;
    private Utils utils;
    private TimeManager timeManager;
    private String format_style, remindBeforeInMins;
    private boolean in24hrsFormat;
    private Context context;
    private BroadcastReceiver smsReceivedListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                // When new sms is received
                if (action.equals(filter.getAction(0))) {
                    // update timeline data
                    setTimeLineData();

                    // Reset Alarm Reminder
                    setAlarmReminder();
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        this.context = getContext();
        // Bind fields
        purchased_tmView = root.findViewById(R.id.purchased_time);
        expiry_tmView = root.findViewById(R.id.expiry_time);
        tm_leftView = root.findViewById(R.id.time_left);

//        Button renew_now = root.findViewById(R.id.renew_now);
        // get preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        in24hrsFormat = preferences.getBoolean("twenty4_hour_clock", false);
        format_style = preferences.getString("format_style", getString(R.string.default_date_format));
        remindBeforeInMins = preferences.getString("remindBeforeInMinutes", getString(R.string.default_reminder_time));
        if (remindBeforeInMins != null) {
            remindBeforeInMins = remindBeforeInMins.split(" ")[0];
        }

        // Set event listener for renew now btn
//        renew_now.setOnClickListener(v -> initRenewProcess());
        // register receiver
        filter = new IntentFilter(context.getString(R.string.action_smsReceiver));
        context.registerReceiver(smsReceivedListener, filter);
        utils = new Utils();
        // Initialize timeManager
        timeManager = new TimeManager(getContext());
        // Populate Timeline fields
        setTimeLineData();

        // Set Alarm Reminder
        setAlarmReminder();
        return root;
    }

    private void setTimeLineData() {
        long purchase_time = timeManager.getPurchase_time();
        long expiry_time = timeManager.getExpiry_time();
        if (purchase_time > 0) {
            String purchase_date = utils.formatDate(purchase_time, format_style, in24hrsFormat);
            String expiry_date = utils.formatDate(expiry_time, format_style, in24hrsFormat);

            purchased_tmView.setText(purchase_date);
            expiry_tmView.setText(expiry_date);

            setTimeLeft(timeManager.getTimeLeftInMillis());
        }
    }

    private void setTimeLeft(long timeLeft) {
        new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimeLeftCountDown(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                tm_leftView.setText(context.getString(R.string.expired));
            }
        }.start();

    }

    private void updateTimeLeftCountDown(long time_left) {
        long days = time_left / (24 * 60 * 60 * 1000);
        long hours = time_left / (60 * 60 * 1000) % 24;
        long minutes = time_left / (60 * 1000) % 60;
        long seconds = time_left / 1000 % 60;
        String formatted_time_left = String.format(Locale.getDefault(), "%02d d, %02d hrs, %02d mins, %02d sec", days, hours, minutes, seconds);
        tm_leftView.setText(formatted_time_left);
    }

    private void setAlarmReminder() {
        // Set notification and time left
        Intent intent = new Intent(getContext(), BroadcastManager.class);
        intent.putExtra("remindBeforeInMins", remindBeforeInMins);
        intent.setAction("android.intent.startAlarm");
        // getBroadCast(context, requestCode, intent, flags)
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);

        int remBeforeInMins = Integer.parseInt(remindBeforeInMins);

        // create time to ring;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeManager.getExpiry_time());
        // time in minutes to remind before to renew before expiry
        // Note the -ve sign is to make it before.
        calendar.add(Calendar.MINUTE, -(remBeforeInMins));
        long alarmStartTime = calendar.getTimeInMillis();
        if (timeManager.isExpired()) {
            // Cancel Alarm when expiry date is passed
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
        } else {
            // Set Alarm
            if (alarmManager != null && timeManager.getTimeLeftInMins() >= remBeforeInMins) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmStartTime, pendingIntent);
            }
            // for cases where time left is less than reminderBeforeTime
            else {
                if (alarmManager != null) {
                    alarmManager.cancel(pendingIntent);
                }
            }
        }

    }


    @Override
    public void onPause() {
        super.onPause();
        // Unregister the receiver to save unnecessary system overhead
        // Paused activities cannot receive broadcasts anyway
        try {
            if (smsReceivedListener != null) {
                context.unregisterReceiver(smsReceivedListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        context.registerReceiver(smsReceivedListener, filter);
    }
}

