package com.dennohpeter.renewdata;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import permissions.dispatcher.PermissionRequest;

/*
 * Houses commonly used date functions
 */
public class Utils {
    // Takes @param context and returns app version as String e.g 1.0
    static String getAppVersion(Context context) {
        String result = "";
        try {
            result = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
                    .versionName;
            result = result.replaceAll("[a-zA-Z]|-", "");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;

    }

    public String formatDate(long dateInMillis, String format_style, boolean in24Hrs) {
        if (in24Hrs) {
            // Replace hh with bigger HH to transform to 24 system
            format_style = format_style.replace("hh", "HH").replace(" aa", "");
        } else {
            // this indicates whether it's AM or PM
            format_style += " aa";
        }
        return new SimpleDateFormat(format_style, Locale.getDefault()).format(new Date(dateInMillis));
    }

    // returns Current date format in  YYYMMDDHHmmss.
    String timestamp() {
        return new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault()).format(new Date());
    }


    // Adds 24 hours to the given date
    private Date add24Hours(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 24);
        return calendar.getTime();
    }

    long add24Hours(long date) {
        return add24Hours(new Date(date)).getTime();
    }

    long currentDate() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public String formatDate(long dateInMillis, String format_style, boolean in24Hrs, String sep) {
        // for format 1
        format_style = format_style.replace(" hh:", sep + "hh:");
        // for format 2
        format_style = format_style.replace(":ss MM", ":ss" + sep + "MM");
        return formatDate(dateInMillis, format_style, in24Hrs);
    }

    public void showRationaleDialog(Context context, String permission_rationale, PermissionRequest request) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(permission_rationale);
        builder.setPositiveButton(R.string.action_ok, (dialog, which) -> request.proceed()).setNegativeButton(context.getString(R.string.not_now), (dialog, which) -> request.cancel()).show();
    }
}
