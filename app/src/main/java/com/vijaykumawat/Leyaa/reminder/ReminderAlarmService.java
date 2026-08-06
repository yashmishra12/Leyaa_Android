package com.vijaykumawat.Leyaa.reminder;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.vijaykumawat.Leyaa.AddReminderActivity;
import com.vijaykumawat.Leyaa.R;
import com.vijaykumawat.Leyaa.data.AlarmReminderContract;

public class ReminderAlarmService extends IntentService {

    private static final String TAG = ReminderAlarmService.class.getSimpleName();

    // If you want each reminder notification separate, use unique IDs.
    // For now we keep one, but you can make it unique by using reminderId.
    private static final int NOTIFICATION_ID = 42;

    public ReminderAlarmService() {
        super(TAG);
    }

    /**
     * IMPORTANT FIX:
     * - requestCode MUST be unique per reminder, otherwise alarms overwrite each other.
     * - Add FLAG_IMMUTABLE for Android 12+
     */
    public static PendingIntent getReminderPendingIntent(Context context, Uri uri) {
        Intent action = new Intent(context, ReminderAlarmService.class);
        action.setData(uri);

        int requestCode = (int) android.content.ContentUris.parseId(uri);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        return PendingIntent.getService(context, requestCode, action, flags);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
        Intent action = new Intent(this, AddReminderActivity.class);
        action.setData(uri);

        int reminderId = (int) android.content.ContentUris.parseId(uri);

        int stackFlags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            stackFlags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent operation = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(action)
                .getPendingIntent(reminderId, stackFlags);

        // Load reminder title
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
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

//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this, "PRIMARY_CHANNEL_ID")
//                        .setContentTitle(getString(R.string.reminder_title))
//                        .setContentText(description)
//                        .setSmallIcon(R.drawable.ic_add_alert_black_24dp)
//                        .setContentIntent(operation)
//                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
//                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
//                        .setAutoCancel(true)
//                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        String appName = getString(R.string.app_name);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, "PRIMARY_CHANNEL_ID")
                        // App name (top line on many devices)
//                        .setSubText(appName)

                        // Title + content
                        .setContentTitle(appName + " • "+getString(R.string.reminder_title))
                        .setContentText(description)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(description))

                        // Logo/icons
                        .setSmallIcon(R.drawable.ic_leyaa_logo_notification) // <-- use your notification-safe icon
//                        .setLargeIcon(android.graphics.BitmapFactory.decodeResource(
//                                getResources(), R.drawable.ic_leyaa_logo_adobe_express))

                        // Behavior
                        .setContentIntent(operation)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setDefaults(NotificationCompat.DEFAULT_ALL) // sound + vibration + lights
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);


        // Optional: make notifications not overwrite each other
        // manager.notify(reminderId, notificationBuilder.build());

        manager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
}
