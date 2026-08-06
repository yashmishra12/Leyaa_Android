package com.vijaykumawat.Leyaa.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.graphics.BitmapFactory;
import androidx.core.app.TaskStackBuilder;

import com.vijaykumawat.Leyaa.AddReminderActivity;
import com.vijaykumawat.Leyaa.R;
import com.vijaykumawat.Leyaa.data.AlarmReminderContract;

public class ReminderAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = ReminderAlarmReceiver.class.getSimpleName();

    private static final int NOTIFICATION_ID = 42;

    public static PendingIntent getReminderPendingIntent(Context context, Uri uri) {
        Intent action = new Intent(context, ReminderAlarmReceiver.class);
        action.setData(uri);

        int requestCode = (int) android.content.ContentUris.parseId(uri);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getBroadcast(context, requestCode, action, flags);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        // Create channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "PRIMARY_CHANNEL_ID",
                    "Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setDescription("Reminder notifications");

            manager.createNotificationChannel(channel);
        }

        Uri uri = intent.getData();
        if (uri == null) return;

        // Deep link to edit/view reminder
        Intent action = new Intent(context, AddReminderActivity.class);
        action.setData(uri);

        int reminderId = (int) android.content.ContentUris.parseId(uri);

        int stackFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            stackFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent operation = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(action)
                .getPendingIntent(reminderId, stackFlags);

        // Load reminder title
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        String description = "";
        try {
            if (cursor != null && cursor.moveToFirst()) {
                description = AlarmReminderContract.getColumnString(
                        cursor,
                        AlarmReminderContract.AlarmReminderEntry.KEY_TITLE
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        String appName = context.getString(R.string.app_name);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, "PRIMARY_CHANNEL_ID")
                        .setContentTitle(appName + " • "+context.getString(R.string.reminder_title))
                        .setContentText(description)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(description))
                        .setSmallIcon(R.drawable.ic_leyaa_logo_notification) 
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_1))
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .setContentIntent(operation)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setDefaults(NotificationCompat.DEFAULT_ALL) 
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);


        manager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
