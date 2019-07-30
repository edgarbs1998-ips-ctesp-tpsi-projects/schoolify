package com.pam.schoolify.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.pam.schoolify.util.database.Task;
import com.pam.schoolify.util.database.User;

import java.util.List;

public class UserSession {
    // Shared preference name
    private static final String PREFERENCE_NAME = "UserSession";
    // Shared preference keys
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";
    // User instance
    private static User user = null;

    @SuppressLint("ApplySharedPref")
    public static void createUserSession(Context context, User user) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();

        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_TOKEN, user.getToken());

        editor.commit();

        UserSession.user = user;
    }

    public static boolean isUserSessionValid(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        String email = sharedPreferences.getString(KEY_EMAIL, null);
        String token = sharedPreferences.getString(KEY_TOKEN, null);

        if (email == null || token == null) {
            return false;
        }

        SQLiteHelper db = new SQLiteHelper(context);
        User tempUser = db.authenticate(email, null, token);

        if (tempUser == null) {
            return false;
        }

        user = tempUser;

        return true;
    }

    @SuppressLint("ApplySharedPref")
    public static void destroyUserSession(Context context) {
        // Cancel tasks reminders before destroy user session
        SQLiteHelper db = new SQLiteHelper(context);
        List<Task> taskList = db.getAllTasks(UserSession.user);
        for (Task task : taskList) {
            NotificationScheduler.cancelTaskReminder(context, task);
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        Editor editor = sharedPreferences.edit();

        editor.clear();
        editor.commit();

        UserSession.user = null;
    }

    public static User getUser() {
        return user;
    }
}
