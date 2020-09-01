package com.dennohpeter.renewdata.ui.home;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.dennohpeter.renewdata.MainActivity;
import com.dennohpeter.renewdata.R;
import com.dennohpeter.renewdata.Utils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

public class AdjustAlarmActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {
    MaterialButton saveAdjustments;
    private TextInputEditText purchasedTimeTv;
    private Utils util;
    private boolean is24HourFormat;
    private String formatStyle;
    private Calendar calendar;
    private long purchased_time;
    private String selected_subscription_plan;
    private AutoCompleteTextView subscriptionPlansTv;
    private SharedPreferences preferences;
    private TextWatcher SaveTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            long newPurchaseTime = calendar.getTimeInMillis();
            String new_selected_subscription_plan = subscriptionPlansTv.getText().toString();
            saveAdjustments.setEnabled(purchased_time != newPurchaseTime || !selected_subscription_plan.equals(new_selected_subscription_plan));

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adjust_alarm_widget);

        subscriptionPlansTv = findViewById(R.id.select_subscription_plan);
        String[] available_subscription_plans =
                getResources().getStringArray(R.array.subscription_plans);
        subscriptionPlansTv.setAdapter(new ArrayAdapter<>(this,
                R.layout.list_item, available_subscription_plans));

        preferences = getSharedPreferences(getString(R.string.app_preferences), MODE_PRIVATE);
        selected_subscription_plan = preferences.getString(getString(R.string.subscription_plan), available_subscription_plans[1]);

        subscriptionPlansTv.setText(selected_subscription_plan, false);
        subscriptionPlansTv.addTextChangedListener(SaveTextWatcher);

        purchasedTimeTv = findViewById(R.id.purchased_time_new);
        purchasedTimeTv.addTextChangedListener(SaveTextWatcher);

        saveAdjustments = findViewById(R.id.saveAdjustments);
        util = new Utils();
        Intent intent = getIntent();

        is24HourFormat = intent.getBooleanExtra("is24HourFormat", false);

        calendar = Calendar.getInstance();
        purchased_time = intent.getLongExtra("purchaseTime", calendar.getTimeInMillis());
        formatStyle = intent.getStringExtra("formatStyle");
        calendar.setTimeInMillis(purchased_time);

        setPurchasedTime(calendar.getTimeInMillis());

        TextInputLayout changePurchasedTimeField = findViewById(R.id.changePurchasedTimeField);
        changePurchasedTimeField.setEndIconOnClickListener(v -> showTimePicker());
        changePurchasedTimeField.setStartIconOnClickListener(v -> showDatePicker());


    }

    private void showDatePicker() {
        DialogFragment datePicker = new DatePickerFragment(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.show(getSupportFragmentManager(), "date picker");
    }

    private void showTimePicker() {
        DialogFragment timePicker = new TimePickerFragment(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                is24HourFormat

        );
        timePicker.show(getSupportFragmentManager(), "time picker");
    }

    public void saveAlarmSettings(View view) {
        long new_purchased_time = calendar.getTimeInMillis();
        String new_selected_subscription_plan = subscriptionPlansTv.getText().toString();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.confirm_changes);
        builder.setMessage(R.string.save_changes_message);
        builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.subscription_plan), new_selected_subscription_plan);
            editor.apply();

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("newPurchasedTime", new_purchased_time);
            startActivity(intent);

        }).setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel())
                .show();

    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        setPurchasedTime(calendar.getTimeInMillis());

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        setPurchasedTime(calendar.getTimeInMillis());
    }

    private void setPurchasedTime(long timeInMillis) {
        purchasedTimeTv.setText(util.formatDate(timeInMillis, formatStyle, is24HourFormat));
    }
}
