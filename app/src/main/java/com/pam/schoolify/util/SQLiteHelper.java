package com.pam.schoolify.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pam.schoolify.util.database.Task;
import com.pam.schoolify.util.database.TaskType;
import com.pam.schoolify.util.database.User;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to handle SQLite database
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    /**
     * Database name
     */
    private static final String DATABASE_NAME = "schoolify";

    /**
     * Database version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Tables names
     */
    private static final String TABLE_USER = "user";
    private static final String TABLE_TASK = "task";
    private static final String TABLE_TASK_TYPE = "task_type";

    /**
     * User table columns
     */
    private static final String USER_KEY_ID = TABLE_USER + "_id";
    private static final String USER_KEY_EMAIL = TABLE_USER + "_email";
    private static final String USER_KEY_NAME = TABLE_USER + "_name";
    private static final String USER_KEY_PASSWORD = TABLE_USER + "_password";
    private static final String USER_KEY_TOKEN = TABLE_USER + "_token";

    /**
     * Task table columns
     */
    private static final String TASK_KEY_ID = TABLE_TASK + "_id";
    private static final String TASK_KEY_USER = TABLE_TASK + "_user";
    private static final String TASK_KEY_TITLE = TABLE_TASK + "_title";
    private static final String TASK_KEY_TYPE = TABLE_TASK + "_type";
    private static final String TASK_KEY_DATETIME = TABLE_TASK + "_datetime";
    private static final String TASK_KEY_DESCRIPTION = TABLE_TASK + "_description";

    /**
     * Task type table columns
     */
    private static final String TASK_TYPE_KEY_ID = TABLE_TASK_TYPE + "_id";
    private static final String TASK_TYPE_KEY_USER = TABLE_TASK_TYPE + "_user";
    private static final String TASK_TYPE_KEY_TYPE = TABLE_TASK_TYPE + "_type";
    private static final String TASK_TYPE_KEY_COLOR = TABLE_TASK_TYPE + "_color";

    /**
     * Constructor
     *
     * @param context context
     */
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statements to create tables
        String sqlCreateTableUser = "CREATE TABLE " + TABLE_USER + " ( " +
                USER_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_KEY_EMAIL + " TEXT, " +
                USER_KEY_NAME + " TEXT, " +
                USER_KEY_PASSWORD + " TEXT, " +
                USER_KEY_TOKEN + " TEXT )";
        db.execSQL(sqlCreateTableUser);

        String sqlCreateTableTask = "CREATE TABLE " + TABLE_TASK + " ( " +
                TASK_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TASK_KEY_USER + " INTEGER, " +
                TASK_KEY_TITLE + " TEXT, " +
                TASK_KEY_TYPE + " INTEGER, " +
                TASK_KEY_DATETIME + " INTEGER, " +
                TASK_KEY_DESCRIPTION + " TEXT )";
        db.execSQL(sqlCreateTableTask);

        String sqlCreateTableTaskType = "CREATE TABLE " + TABLE_TASK_TYPE + " ( " +
                TASK_TYPE_KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TASK_TYPE_KEY_USER + " INTEGER, " +
                TASK_TYPE_KEY_TYPE + " TEXT, " +
                TASK_TYPE_KEY_COLOR + " TEXT )";
        db.execSQL(sqlCreateTableTaskType);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop current tables if exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK_TYPE);

        // Create fresh user table
        this.onCreate(db);
    }

    /**
     * Table User CRUD (Create, Retrieve, Update, Delete) operations methods
     */

    public User addUser(User user) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Create content values to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(USER_KEY_EMAIL, user.getEmail());
        values.put(USER_KEY_NAME, user.getName());
        values.put(USER_KEY_PASSWORD, user.getPassword());
        values.put(USER_KEY_TOKEN, user.createToken());

        // Insert row
        long id = db.insert(TABLE_USER, null, values);
        user.setId(id);

        // Close database reference
        db.close();

        return user;
    }

    public User authenticate(String email, String password) {
        return authenticate(email, password, null);
    }

    public User authenticate(String email, String password, String token) {
        // Get reference to readable database
        SQLiteDatabase db = this.getReadableDatabase();

        // Build query
        String selection;
        String[] selectionArgs;
        if (token == null) {
            selection = USER_KEY_EMAIL + " = ? AND " + USER_KEY_PASSWORD + " = ?";
            selectionArgs = new String[]{email, Util.sha256Hex(password)};
        } else {
            selection = USER_KEY_EMAIL + " = ? AND " + USER_KEY_TOKEN + " = ?";
            selectionArgs = new String[]{email, token};
        }

        Cursor cursor =
                db.query(TABLE_USER,
                        new String[]{USER_KEY_ID, USER_KEY_EMAIL, USER_KEY_NAME, USER_KEY_PASSWORD, USER_KEY_TOKEN},
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null,
                        null
                );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                User tempUser = new User(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4)
                );

                // Close database and cursor reference
                cursor.close();
                db.close();

                return tempUser;
            }

            // Close cursor reference
            cursor.close();
        }

        // Close database reference
        db.close();

        // If password does not match or no record with specified email
        return null;
    }

    public boolean isEmailUsed(String email) {
        // Get reference to readable database
        SQLiteDatabase db = this.getReadableDatabase();

        // Build query
        Cursor cursor =
                db.query(TABLE_USER,
                        new String[]{USER_KEY_ID},
                        USER_KEY_EMAIL + " = ?",
                        new String[]{email},
                        null,
                        null,
                        null,
                        null
                );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Close database and cursor reference
                cursor.close();
                db.close();
                return true;
            }

            // Close cursor reference
            cursor.close();
        }

        // Close database reference
        db.close();

        // Email does not exist
        return false;
    }

    public User updateUser(User user) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Create content values to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(USER_KEY_EMAIL, user.getEmail());
        values.put(USER_KEY_NAME, user.getName());
        values.put(USER_KEY_TOKEN, user.createToken());

        // Update row
        int i = db.update(TABLE_USER,
                values,
                USER_KEY_ID + " = ?",
                new String[]{String.valueOf(user.getId())});

        // Close database reference
        db.close();

        if (i == 0) {
            return null;
        } else {
            return user;
        }
    }

    public User changePassword(User user) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Create content values to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(USER_KEY_PASSWORD, user.getPassword());

        // Update row
        int i = db.update(TABLE_USER,
                values,
                USER_KEY_ID + " = ?",
                new String[]{String.valueOf(user.getId())});

        // Close database reference
        db.close();

        if (i == 0) {
            return null;
        } else {
            return user;
        }
    }

    public boolean deleteUser(User user) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete associated tasks
        db.delete(TABLE_TASK,
                TASK_KEY_USER + " = ?",
                new String[]{String.valueOf(user.getId())});

        // Delete associated task types
        db.delete(TABLE_TASK_TYPE,
                TASK_TYPE_KEY_USER + " = ?",
                new String[]{String.valueOf(user.getId())});

        // Delete row
        int i = db.delete(TABLE_USER,
                USER_KEY_ID + " = ?",
                new String[]{String.valueOf(user.getId())});

        // Close database reference
        db.close();

        return i != 0;
    }

    /**
     * Table Task CRUD (Create, Retrieve, Update, Delete) operations methods
     */

    public Task addTask(Task task) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Create content values to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(TASK_KEY_USER, task.getUser());
        values.put(TASK_KEY_TITLE, task.getTitle());
        values.put(TASK_KEY_TYPE, task.getType());
        values.put(TASK_KEY_DATETIME, task.getDateTime());
        values.put(TASK_KEY_DESCRIPTION, task.getDescription());

        // Insert row
        long id = db.insert(TABLE_TASK, null, values);
        task.setId(id);

        // Close database reference
        db.close();

        return task;
    }

    public Task getTask(long id) {
        // Get reference to readable database
        SQLiteDatabase db = this.getReadableDatabase();

        // Build query
        Cursor cursor =
                db.query(TABLE_TASK,
                        new String[]{TASK_KEY_ID, TASK_KEY_USER, TASK_KEY_TITLE, TASK_KEY_TYPE, TASK_KEY_DATETIME, TASK_KEY_DESCRIPTION},
                        TASK_KEY_ID + " = ?",
                        new String[]{Long.toString(id)},
                        null,
                        null,
                        null,
                        null
                );

        Task task = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                task = new Task(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getLong(3),
                        cursor.getLong(4),
                        cursor.getString(5)
                );
            }

            // Close cursor reference
            cursor.close();
        }

        // Close database reference
        db.close();

        return task;
    }

    public List<Task> getAllTasks(User user) {
        // Create list to return
        List<Task> taskList = new LinkedList<>();

        // Get reference to readable database
        SQLiteDatabase db = this.getReadableDatabase();

        // Build query
        Cursor cursor =
                db.query(TABLE_TASK,
                        new String[]{TASK_KEY_ID, TASK_KEY_USER, TASK_KEY_TITLE, TASK_KEY_TYPE, TASK_KEY_DATETIME, TASK_KEY_DESCRIPTION},
                        TASK_KEY_USER + " = ?",
                        new String[]{Long.toString(user.getId())},
                        null,
                        null,
                        TASK_KEY_DATETIME + " ASC",
                        null
                );

        Task task;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    task = new Task(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getLong(3),
                            cursor.getLong(4),
                            cursor.getString(5)
                    );

                    taskList.add(task);
                } while (cursor.moveToNext());
            }

            // Close cursor reference
            cursor.close();
        }

        // Close database reference
        db.close();

        return taskList;
    }

    public List<Task> getTasksByDate(User user, Calendar calendar) {
        // Create list to return
        List<Task> taskList = new LinkedList<>();

        // Get reference to readable database
        SQLiteDatabase db = this.getReadableDatabase();

        // Get start and end of day in millis
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDayInMillis = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDayInMillis = calendar.getTimeInMillis();

        // Build query
        Cursor cursor =
                db.query(TABLE_TASK,
                        new String[]{TASK_KEY_ID, TASK_KEY_USER, TASK_KEY_TITLE, TASK_KEY_TYPE, TASK_KEY_DATETIME, TASK_KEY_DESCRIPTION},
                        TASK_KEY_USER + " = ? AND " + TASK_KEY_DATETIME + " BETWEEN ? AND ?",
                        new String[]{String.valueOf(user.getId()), String.valueOf(startOfDayInMillis), String.valueOf(endOfDayInMillis)},
                        null,
                        null,
                        TASK_KEY_DATETIME + " ASC",
                        null
                );

        Task task;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    task = new Task(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getLong(3),
                            cursor.getLong(4),
                            cursor.getString(5)
                    );

                    taskList.add(task);
                } while (cursor.moveToNext());
            }

            // Close cursor reference
            cursor.close();
        }

        // Close database reference
        db.close();

        return taskList;
    }

    public List<Task> getFutureTasks(User user, Calendar calendar) {
        // Create list to return
        List<Task> taskList = new LinkedList<>();

        // Get reference to readable database
        SQLiteDatabase db = this.getReadableDatabase();

        // Build query
        Cursor cursor =
                db.query(TABLE_TASK,
                        new String[]{TASK_KEY_ID, TASK_KEY_USER, TASK_KEY_TITLE, TASK_KEY_TYPE, TASK_KEY_DATETIME, TASK_KEY_DESCRIPTION},
                        TASK_KEY_USER + " = ? AND " + TASK_KEY_DATETIME + " >= ?",
                        new String[]{String.valueOf(user.getId()), String.valueOf(calendar.getTimeInMillis())},
                        null,
                        null,
                        TASK_KEY_DATETIME + " ASC",
                        null
                );

        Task task;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    task = new Task(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getLong(3),
                            cursor.getLong(4),
                            cursor.getString(5)
                    );

                    taskList.add(task);
                } while (cursor.moveToNext());
            }

            // Close cursor reference
            cursor.close();
        }

        // Close database reference
        db.close();

        return taskList;
    }

    public List<Task> getTasksByType(User user, TaskType taskType) {
        // Create list to return
        List<Task> taskList = new LinkedList<>();

        // Get reference to readable database
        SQLiteDatabase db = this.getReadableDatabase();

        // Build query
        Cursor cursor =
                db.query(TABLE_TASK,
                        new String[]{TASK_KEY_ID, TASK_KEY_USER, TASK_KEY_TITLE, TASK_KEY_TYPE, TASK_KEY_DATETIME, TASK_KEY_DESCRIPTION},
                        TASK_KEY_USER + " = ? AND " + TASK_KEY_TYPE + " = ?",
                        new String[]{String.valueOf(user.getId()), String.valueOf(taskType.getId())},
                        null,
                        null,
                        TASK_KEY_DATETIME + " ASC",
                        null
                );

        Task task;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    task = new Task(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getLong(3),
                            cursor.getLong(4),
                            cursor.getString(5)
                    );

                    taskList.add(task);
                } while (cursor.moveToNext());
            }

            // Close cursor reference
            cursor.close();
        }

        // Close database reference
        db.close();

        return taskList;
    }

    public Task updateTask(Task task) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Create content values to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(TASK_KEY_TITLE, task.getTitle());
        values.put(TASK_KEY_TYPE, task.getType());
        values.put(TASK_KEY_DATETIME, task.getDateTime());
        values.put(TASK_KEY_DESCRIPTION, task.getDescription());

        // Update row
        int i = db.update(TABLE_TASK,
                values,
                TASK_KEY_ID + " = ?",
                new String[]{String.valueOf(task.getId())});

        // Close database reference
        db.close();

        if (i == 0) {
            return null;
        } else {
            return task;
        }
    }

    public boolean deleteTask(Task task) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete row
        int i = db.delete(TABLE_TASK,
                TASK_KEY_ID + " = ?",
                new String[]{String.valueOf(task.getId())});

        // Close database reference
        db.close();

        return i != 0;
    }

    /**
     * Table Task Type CRUD (Create, Retrieve, Update, Delete) operations methods
     */

    public TaskType addTaskType(TaskType taskType) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Create content values to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(TASK_TYPE_KEY_USER, taskType.getUser());
        values.put(TASK_TYPE_KEY_TYPE, taskType.getType());
        values.put(TASK_TYPE_KEY_COLOR, taskType.getColor());

        // Insert row
        long id = db.insert(TABLE_TASK_TYPE, null, values);
        taskType.setId(id);

        // Close database reference
        db.close();

        return taskType;
    }

    public boolean isTaskTypeUsed(User user, String type) {
        // Get reference to readable database
        SQLiteDatabase db = this.getReadableDatabase();

        // Build query
        Cursor cursor =
                db.query(TABLE_TASK_TYPE,
                        new String[]{TASK_TYPE_KEY_ID},
                        TASK_TYPE_KEY_TYPE + " = ? AND " + TASK_TYPE_KEY_USER + " = ?",
                        new String[]{type, Long.toString(user.getId())},
                        null,
                        null,
                        null,
                        null
                );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                // Close database and cursor reference
                cursor.close();
                db.close();
                return true;
            }

            // Close cursor reference
            cursor.close();
        }

        // Close database reference
        db.close();

        // Email does not exist
        return false;
    }

    public TaskType getTaskType(long id) {
        // Get reference to readable database
        SQLiteDatabase db = this.getReadableDatabase();

        // Build query
        Cursor cursor =
                db.query(TABLE_TASK_TYPE,
                        new String[]{TASK_TYPE_KEY_ID, TASK_TYPE_KEY_USER, TASK_TYPE_KEY_TYPE, TASK_TYPE_KEY_COLOR},
                        TASK_TYPE_KEY_ID + " = ?",
                        new String[]{String.valueOf(id)},
                        null,
                        null,
                        null,
                        null
                );

        TaskType taskType = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                taskType = new TaskType(
                        cursor.getLong(0),
                        cursor.getLong(1),
                        cursor.getString(2),
                        cursor.getString(3)
                );
            }

            // Close cursor reference
            cursor.close();
        }

        // Close database reference
        db.close();

        return taskType;
    }

    public List<TaskType> getAllTaskTypes(User user) {
        // Create list to return
        List<TaskType> taskTypeList = new LinkedList<>();

        // Get reference to readable database
        SQLiteDatabase db = this.getReadableDatabase();

        // Build query
        Cursor cursor =
                db.query(TABLE_TASK_TYPE,
                        new String[]{TASK_TYPE_KEY_ID, TASK_TYPE_KEY_USER, TASK_TYPE_KEY_TYPE, TASK_TYPE_KEY_COLOR},
                        TASK_TYPE_KEY_USER + " = ?",
                        new String[]{Long.toString(user.getId())},
                        null,
                        null,
                        TASK_TYPE_KEY_TYPE + " ASC",
                        null
                );

        TaskType taskType;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    taskType = new TaskType(
                            cursor.getLong(0),
                            cursor.getLong(1),
                            cursor.getString(2),
                            cursor.getString(3)
                    );

                    taskTypeList.add(taskType);
                } while (cursor.moveToNext());
            }

            // Close cursor reference
            cursor.close();
        }

        // Close database reference
        db.close();

        return taskTypeList;
    }

    public TaskType updateTaskType(TaskType taskType) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Create content values to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(TASK_TYPE_KEY_TYPE, taskType.getType());
        values.put(TASK_TYPE_KEY_COLOR, taskType.getColor());

        // Update row
        int i = db.update(TABLE_TASK_TYPE,
                values,
                TASK_TYPE_KEY_ID + " = ?",
                new String[]{String.valueOf(taskType.getId())});

        // Close database reference
        db.close();

        if (i == 0) {
            return null;
        } else {
            return taskType;
        }
    }

    public boolean deleteTaskType(TaskType taskType) {
        // Get reference to writable database
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete associated tasks
        db.delete(TABLE_TASK,
                TASK_KEY_TYPE + " = ?",
                new String[]{String.valueOf(taskType.getId())});

        // Delete row
        int i = db.delete(TABLE_TASK_TYPE,
                TASK_TYPE_KEY_ID + " = ?",
                new String[]{String.valueOf(taskType.getId())});

        // Close database reference
        db.close();

        return i != 0;
    }
}
