package com.vijaykumawat.Leyaa.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;

public class AlarmScheduler {

    public void setAlarm(Context context, long alarmTime, Uri reminderTask) {
        AlarmManager manager = AlarmManagerProvider.getAlarmManager(context);

        PendingIntent operation =
                ReminderAlarmService.getReminderPendingIntent(context, reminderTask);



        if (manager == null) return;

        long now = System.currentTimeMillis();
        if (alarmTime <= now) alarmTime = now + 5_000;

        // Android 12+ can block exact alarms unless allowed by user/device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!manager.canScheduleExactAlarms()) {
                // fallback (not exact, but won't crash)
                manager.set(AlarmManager.RTC_WAKEUP, alarmTime, operation);
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, operation);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, operation);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, alarmTime, operation);
        }
    }

    /**
     * IMPORTANT:
     * Do NOT use AlarmManager.setRepeating() (inexact on newer Android + delayed in Doze).
     *
     * This schedules ONLY the first exact alarm.
     * When the alarm fires, you must schedule the next one again from
     * ReminderAlarmService / Receiver using setNextRepeat(...)
     */
    public void setRepeatAlarm(Context context, long alarmTime, Uri reminderTask, long repeatTime) {
        // Schedule the first one exact
        setAlarm(context, alarmTime, reminderTask);
        // repeatTime will be used when alarm triggers (see setNextRepeat below)
    }

    /**
     * Call this method from your alarm handler (ReminderAlarmService / Receiver)
     * AFTER you show notification, to schedule the next repeat.
     */
    public void setNextRepeat(Context context, Uri reminderTask, long repeatTime) {
        long nextTime = System.currentTimeMillis() + repeatTime;
        setAlarm(context, nextTime, reminderTask);
    }

    public void cancelAlarm(Context context, Uri reminderTask) {
        AlarmManager manager = AlarmManagerProvider.getAlarmManager(context);
        PendingIntent operation =
                ReminderAlarmService.getReminderPendingIntent(context, reminderTask);

        if (manager == null) return;

        manager.cancel(operation);
        operation.cancel();
    }
}
