package com.dennohpeter.renewdata.ui.settings;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;

import com.dennohpeter.renewdata.R;
import com.dennohpeter.renewdata.Utils;

import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class SettingsFragment extends PreferenceFragmentCompat {
    private final String READ_PHONE_STATE_PERMISSION = Manifest.permission.READ_PHONE_STATE;
    private Context context;
    private Utils utils;
    private ListPreference availableNetworks;
    private Preference selectSimCard;
    private PreferenceCategory networkPreferenceCategory;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
        this.context = getContext();
        this.utils = new Utils();
        // Recommend to friends
        Preference recommendToFriends = findPreference("recommend_to_friends");
        if (recommendToFriends != null) {
            recommendToFriends.setOnPreferenceClickListener(preference -> {
                String body = "Check Out " + getString(R.string.app_name) +
                        "!, a simple and efficient reminder app to help you manage mobile data efficiently.\n" +
                        "Get " + getString(R.string.app_name) + ":\n" + getString(R.string.app_source);
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, body);
                intent.setType("text/plain");

                startActivity(Intent.createChooser(intent, "Share via"));
                return true;
            });
        }
        networkPreferenceCategory = findPreference(getString(R.string.networks));
        availableNetworks = new ListPreference(context);
        selectSimCard = findPreference(getString(R.string.selected_network));

        if (selectSimCard != null) {
            selectSimCard.setOnPreferenceClickListener(preference -> {
                SettingsFragmentPermissionsDispatcher.setSimCardOperatorsWithPermissionCheck(this);
                return true;
            });
        }
    }

    @NeedsPermission(READ_PHONE_STATE_PERMISSION)
    void setSimCardOperators() {
        List<String> simCardList = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT > 22) {
                //for dual sim mobile
                SubscriptionManager localSubscriptionManager = SubscriptionManager.from(getContext());
                if (localSubscriptionManager.getActiveSubscriptionInfoCount() > 1) {
                    //if there are two sims in dual sim mobile
                    List<SubscriptionInfo> localList = localSubscriptionManager.getActiveSubscriptionInfoList();
                    SubscriptionInfo simInfo = localList.get(0);
                    SubscriptionInfo simInfo1 = localList.get(1);

                    final String sim1 = simInfo.getCarrierName().toString();
                    final String sim2 = simInfo1.getCarrierName().toString();
                    simCardList.add(sim1);
                    simCardList.add(sim2);

                } else {
                    //if there is 1 sim in dual sim mobile
                    TelephonyManager tManager = (TelephonyManager) context
                            .getSystemService(Context.TELEPHONY_SERVICE);

                    String sim1 = tManager.getNetworkOperatorName();
                    simCardList.add(sim1);
                }
            } else {
                //below android version 22
                TelephonyManager tManager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);

                String sim1 = tManager.getNetworkOperatorName();
                simCardList.add(sim1);

            }
            Log.d("TAG", "setSimCardOperators: " + simCardList);
            networkPreferenceCategory.removePreference(selectSimCard);

            availableNetworks.setKey(getString(R.string.selected_network));
            availableNetworks.setTitle(R.string.select_network);
            availableNetworks.setDialogTitle(R.string.select_network);
            availableNetworks.setSummary(R.string.select_default_network);
            availableNetworks.setIcon(R.drawable.ic_network);

            CharSequence[] simCardOperators = simCardList.toArray(new CharSequence[0]);
            availableNetworks.setEntries(simCardOperators);
            availableNetworks.setEntryValues(simCardOperators);

            if (availableNetworks.getValue() == null) {
                availableNetworks.setValueIndex(0);
            }

            networkPreferenceCategory.addPreference(availableNetworks);
            getPreferenceManager().showDialog(availableNetworks);
        }
    }


    @OnShowRationale(READ_PHONE_STATE_PERMISSION)
    void showRationaleForPhoneSTATE(PermissionRequest request) {
        utils.showRationaleDialog(context, getString(R.string.permission_phone_state_rationale, getString(R.string.app_name)), request);
    }

    @OnPermissionDenied(READ_PHONE_STATE_PERMISSION)
    void onPhoneSTATEDenied() {
        Toast.makeText(context, R.string.permission_phone_state_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(READ_PHONE_STATE_PERMISSION)
    void onPhoneSTATENeverAskAgain() {
        Toast.makeText(context, R.string.permission_phone_state_never_ask_again, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SettingsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

}
