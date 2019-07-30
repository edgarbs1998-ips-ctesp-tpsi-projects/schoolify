package com.pam.schoolify.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;

import com.pam.schoolify.util.database.Task;
import com.pam.schoolify.util.database.User;

import java.util.Calendar;
import java.util.List;

/**
 * Handle fired notifications and device boot
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Set task reminders after device boot
        if (intent.getAction() != null && context != null) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
                User user = UserSession.getUser();
                if (user != null) {
                    // Set tasks reminders
                    SQLiteHelper db = new SQLiteHelper(context);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    List<Task> taskList = db.getFutureTasks(user, calendar);

                    for (Task task : taskList) {
                        NotificationScheduler.setTaskReminder(context, task);
                    }

                    return;
                }
            }
        }

        // Get notification intent data
        int requestCode = intent.getIntExtra(NotificationScheduler.EXTRA_REQUEST_CODE, 0);
        long taskId = intent.getLongExtra(NotificationScheduler.EXTRA_TASK_ID, 0);
        String title = intent.getStringExtra(NotificationScheduler.EXTRA_TASK_TITLE);
        String type = intent.getStringExtra(NotificationScheduler.EXTRA_TASK_TYPE);
        long dateTime = intent.getLongExtra(NotificationScheduler.EXTRA_TASK_DATETIME, 0);

        String contentDateTime = DateFormat.getDateFormat(context).format(dateTime) + " " + DateFormat.getTimeFormat(context).format(dateTime);

        // Trigger the notification
        NotificationScheduler.showTaskNotification(context, requestCode, taskId, title, type + " - " + contentDateTime);
    }
}
