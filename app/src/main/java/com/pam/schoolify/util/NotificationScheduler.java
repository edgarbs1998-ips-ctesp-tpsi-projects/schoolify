package com.pam.schoolify.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.pam.schoolify.R;
import com.pam.schoolify.activity.SettingsActivity;
import com.pam.schoolify.activity.TaskDetailActivity;
import com.pam.schoolify.fragment.TaskDetailFragment;
import com.pam.schoolify.util.database.Task;
import com.pam.schoolify.util.database.TaskType;

import java.util.Calendar;

public class NotificationScheduler {

    public static final String EXTRA_REQUEST_CODE = "request_code";
    public static final String EXTRA_TASK_ID = "task_id";
    public static final String EXTRA_TASK_TITLE = "task_title";
    public static final String EXTRA_TASK_TYPE = "task_type";
    public static final String EXTRA_TASK_DATETIME = "task_datetime";

    private static final String NOTIFICATION_CHANNEL_FUTURE_TASKS = "future_tasks";
    private static final int NOTIFICATION_TASKS_BASE_REQUEST_CODE = 100;

    public static void setTaskReminder(Context context, Task task) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean settingsNotificationsFutureTasks = sharedPreferences.getBoolean(SettingsActivity.KEY_NOTIFICATIONS_FUTURE_TASKS, true);

        if (!settingsNotificationsFutureTasks) {
            return;
        }

        // Cancel already scheduled task reminders
        cancelTaskReminder(context, task);

        int settingsNotificationsFutureTasksReminderTime = Integer.parseInt(sharedPreferences.getString(SettingsActivity.KEY_NOTIFICATIONS_FUTURE_TASKS_REMINDER_TIME, "180"));

        Calendar calendar = Calendar.getInstance();

        Calendar setCalendar = Calendar.getInstance();
        setCalendar.setTimeInMillis(task.getDateTime());
        setCalendar.add(Calendar.MINUTE, -settingsNotificationsFutureTasksReminderTime);

        if (setCalendar.before(calendar)) {
            return;
        }

        // Enable a receiver
        ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
        PackageManager packageManager = context.getPackageManager();

        packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        int requestCode = NOTIFICATION_TASKS_BASE_REQUEST_CODE + (int) task.getId();

        SQLiteHelper db = new SQLiteHelper(context);
        TaskType taskType = db.getTaskType(task.getType());

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_REQUEST_CODE, requestCode);
        intent.putExtra(EXTRA_TASK_ID, task.getId());
        intent.putExtra(EXTRA_TASK_TITLE, task.getTitle());
        intent.putExtra(EXTRA_TASK_TYPE, taskType.getType());
        intent.putExtra(EXTRA_TASK_DATETIME, task.getDateTime());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC, setCalendar.getTimeInMillis(), pendingIntent);
        }
    }

    public static void cancelTaskReminder(Context context, Task task) {
        // Disable a receiver
        ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
        PackageManager packageManager = context.getPackageManager();

        packageManager.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_TASKS_BASE_REQUEST_CODE + (int) task.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
        pendingIntent.cancel();
    }

    public static void showTaskNotification(Context context, int requestCode, long taskId, String title, String content) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String settingsNotificationsFutureTasksRingtone = sharedPreferences.getString(SettingsActivity.KEY_NOTIFICATIONS_FUTURE_TASKS_RINGTONE, "content://settings/system/notification_sound");
        Uri alarmSound = Uri.parse(settingsNotificationsFutureTasksRingtone);

        Intent notificationIntent = new Intent(context, TaskDetailActivity.class);
        notificationIntent.putExtra(TaskDetailFragment.ARG_ITEM_ID, taskId);
        notificationIntent.putExtra(TaskDetailFragment.ARG_NOTIFICTION, true);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(TaskDetailActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_FUTURE_TASKS, context.getString(R.string.notifications_channel_future_tasks_title), NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription(context.getString(R.string.notifications_channel_future_tasks_description));
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_FUTURE_TASKS);

        boolean settingsNotificationsFutureTasksVibrate = sharedPreferences.getBoolean(SettingsActivity.KEY_NOTIFICATIONS_FUTURE_TASKS_VIBRATE, true);
        int notificationDefaults = Notification.DEFAULT_LIGHTS;
        if (settingsNotificationsFutureTasksVibrate) {
            notificationDefaults |= Notification.DEFAULT_VIBRATE;
        }

        Notification notification = builder.setContentTitle(title)
                .setDefaults(notificationDefaults)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent).build();

        notificationManager.notify(requestCode, notification);

    }
}
