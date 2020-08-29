package com.dennohpeter.renewdata;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.TimeUnit;

public class TimeManager {
    private Utils utils;
    private long purchase_time;
    private long expiry_time;
    private DatabaseHelper databaseHelper;

    public TimeManager(Context context) {
        databaseHelper = new DatabaseHelper(context);
        utils = new Utils();

        // Setting Expiry and Purchase date from messages
        setFieldMembers(context);
    }

    public long getPurchase_time() {
        return purchase_time;
    }

    public long getExpiry_time() {
        return expiry_time;
    }

    public long getTimeLeftInMillis() {
        long currentTime = utils.currentDate();
        return expiry_time - currentTime;
    }

    public int getTimeLeftInMins() {
        return (int) TimeUnit.MILLISECONDS.toMinutes(getTimeLeftInMillis());
    }

    public boolean isExpired() {
        return getTimeLeftInMillis() < 0;
    }

    private void setFieldMembers(Context context) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = databaseHelper.getLogMessages(db);
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndex("msg_body")).contains(context.getString(R.string.subscribed))) {
                this.purchase_time = cursor.getLong(cursor.getColumnIndex("received_date"));
                this.expiry_time = utils.add24Hours(purchase_time);
                break;
            }
        }
        cursor.close();
        db.close();
    }
}
