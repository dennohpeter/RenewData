package com.dennohpeter.renewdata.ui.home;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dennohpeter.renewdata.R;

public class AdjustAlarmActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adjust_alarm_widget);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AutoCompleteTextView subscription_plan = findViewById(R.id.select_subscription_plan);
        subscription_plan.setAdapter(new ArrayAdapter<>(this,
                R.layout.list_item,
                getResources().getStringArray(R.array.subscription_plans)));

    }
}
