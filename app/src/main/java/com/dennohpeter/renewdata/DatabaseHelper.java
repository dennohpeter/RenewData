package com.dennohpeter.renewdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "renew_data.db";
    private static final String LOGS_QUERY = "CREATE TABLE logs_table (msg_from TEXT, msg_body TEXT, received_date INTEGER UNIQUE);";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(LOGS_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void create_or_update_logs(String msg_from, String msg_body, long received_date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("msg_from", msg_from);
        contentValues.put("msg_body", msg_body);
        contentValues.put("received_date", received_date);
        int id = (int) db.insertWithOnConflict("logs_table", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        if (id == -1) {
            db.update("logs_table", contentValues, "received_date=?", new String[]{String.valueOf(received_date)});
        }
        db.close();
    }

    public Cursor getLogMessages(SQLiteDatabase db) {
        String[] columns = {"msg_from", "msg_body", "received_date"};
        String orderBy = "received_date " + "DESC";
        return db.query("logs_table", columns, null, null, null, null, orderBy);
    }

}
