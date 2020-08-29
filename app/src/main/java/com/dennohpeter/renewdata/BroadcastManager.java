package com.dennohpeter.renewdata;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BroadcastManager extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // initialize DatabaseHelper
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        int notificationId = 0;
        if (action != null) {
            switch (action) {
                case Telephony.Sms.Intents.SMS_RECEIVED_ACTION:
                    // retrieve the SMS message received and pass it to the
                    for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                        String msg_from = smsMessage.getOriginatingAddress();
                        String msg_body = smsMessage.getMessageBody();
                        long timestampMillis = smsMessage.getTimestampMillis();
                        // check if it's from Telkom
                        if (msg_from != null && msg_from.toLowerCase().contains(context.getString(R.string.telkom).toLowerCase())) {
                            // Save it  to db
                            databaseHelper.create_or_update_logs(msg_from, msg_body, timestampMillis);
                            context.sendBroadcast(new Intent(context.getString(R.string.action_smsReceiver)));
                        }
                    }
                    break;
                case "android.intent.startAlarm":
                    // create notificationID
                    notificationId = (int) System.currentTimeMillis();
                    // Get remaining time from intent
                    int remindBeforeInMins = intent.getIntExtra("remindBeforeInMins", 0);

                    // when notification is tapped call MainActivity
                    Intent homeTabIntent = new Intent(context, MainActivity.class);
                    PendingIntent HomeTabPendingIntent = PendingIntent.getActivity(context, 0, homeTabIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    // When  Renew Later action is clicked call Send BroadCast to snooze
                    Intent dismissIntent = new Intent(context, BroadcastManager.class);
                    dismissIntent.setAction("dismissReminder");
                    dismissIntent.putExtra("notificationId", notificationId);
                    PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, notificationId, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    // Choose notification color based on time left
                    int color;
                    if (remindBeforeInMins < 10) {
                        color = Color.RED;
                    } else {
                        color = Color.GREEN;
                    }
                    // Prepare Notification
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "renew_reminder")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Renew Data Reminder")
                            .setContentText(remindBeforeInMins + " minutes left before data expires, Renew now.")
                            .setWhen(System.currentTimeMillis())
                            .setAutoCancel(true)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setColor(color)
                            .setContentIntent(HomeTabPendingIntent)
                            .addAction(R.drawable.ic_snooze, "Remind Later", dismissPendingIntent);
//                            .addAction(R.drawable.ic_touch, "Renew Now", renewNoPendingIntent);
                    // Notify
                    NotificationManagerCompat.from(context).notify(notificationId, builder.build());
                    break;
                case "dismissReminder":
                    // dismiss reminder
                    notificationId = intent.getIntExtra("notificationId", notificationId);
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (manager != null) {
                        manager.cancel(notificationId);
                    }
                    Toast.makeText(context, "Renew Reminder Dismissed", Toast.LENGTH_SHORT).show();

            }
        }
    }
}
