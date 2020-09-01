package com.dennohpeter.renewdata.ui.home;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment {
    private int hourOfDay, minute;
    private boolean is24HourFormat;

    public TimePickerFragment(int hourOfDay, int minute, boolean is24HourFormat) {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
        this.is24HourFormat = is24HourFormat;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener) getActivity(), hourOfDay, minute, is24HourFormat);
    }
}
