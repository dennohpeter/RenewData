package com.dennohpeter.renewdata.ui.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.dennohpeter.renewdata.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
        // Recommend to friends
        Preference shareApp = findPreference("recommend_to_friends");
        if (shareApp != null) {
            shareApp.setOnPreferenceClickListener(preference -> {
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
    }
}
