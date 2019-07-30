package com.pam.schoolify.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.pam.schoolify.R;
import com.pam.schoolify.fragment.TaskListFragment;
import com.pam.schoolify.util.NotificationScheduler;
import com.pam.schoolify.util.SQLiteHelper;
import com.pam.schoolify.util.UserSession;
import com.pam.schoolify.util.Util;
import com.pam.schoolify.util.database.Task;
import com.pam.schoolify.util.database.TaskType;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Activity to add new tasks
 */
public class AddTaskActivity extends AppCompatActivity {

    /**
     * Request codes for startActivityForResult
     */
    private static final int REQUEST_ADD_TASK_TYPE = 1;
    private static final int REQUEST_EDIT_TASK_TYPE = 2;

    /**
     * AsyncTask add task handler
     */
    private AddTaskTask addTaskTask = null;

    /**
     * Database handler
     */
    private SQLiteHelper db;

    /**
     * Calendar to handle task datetime
     */
    private Calendar calendar;

    /**
     * Date and time pickers listeners
     */
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private TimePickerDialog.OnTimeSetListener timeSetListener;

    /**
     * UI references
     */
    private TextInputLayout textInputLayoutTitle;
    private TextInputEditText editTextTitle;
    private View viewTypeColor;
    private Spinner spinnerType;
    private TextInputLayout textInputLayoutDate;
    private TextInputEditText editTextDate;
    private TextInputLayout textInputLayoutTime;
    private TextInputEditText editTextTime;
    private TextInputLayout textInputLayoutDescription;
    private TextInputEditText editTextDescription;
    private View viewForm;
    private View viewProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Show the Up button in the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialize database handler
        db = new SQLiteHelper(AddTaskActivity.this);

        // Initialize calendar
        calendar = Calendar.getInstance();

        // Obtain UI references
        textInputLayoutTitle = findViewById(R.id.textInputLayoutTitle);
        editTextTitle = findViewById(R.id.editTextTitle);
        viewTypeColor = findViewById(R.id.viewTypeColor);
        spinnerType = findViewById(R.id.spinnerType);
        textInputLayoutDate = findViewById(R.id.textInputLayoutDate);
        editTextDate = findViewById(R.id.editTextDate);
        textInputLayoutTime = findViewById(R.id.textInputLayoutTime);
        editTextTime = findViewById(R.id.editTextTime);
        textInputLayoutDescription = findViewById(R.id.textInputLayoutDescription);
        editTextDescription = findViewById(R.id.editTextDescription);
        viewProgressBar = findViewById(R.id.progressBarAddTask);
        viewForm = findViewById(R.id.formAddTask);

        // Spinner item selected listener
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TaskType taskType = (TaskType) parent.getItemAtPosition(position);
                viewTypeColor.setBackgroundColor(taskType.getColor());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Get activity intent extras
        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            // Get CalendarFragment clicked date
            long selectedDateInMillis = intent.getLongExtra(TaskListFragment.EXTRA_SELECTED_DATE, System.currentTimeMillis());
            calendar.setTimeInMillis(selectedDateInMillis);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            editTextDate.setText(DateFormat.getDateFormat(AddTaskActivity.this).format(calendar.getTime()));
        }

        // Date picker set listener
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                editTextDate.setText(DateFormat.getDateFormat(AddTaskActivity.this).format(calendar.getTime()));
            }
        };

        // Time picker set listener
        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                editTextTime.setText(DateFormat.getTimeFormat(AddTaskActivity.this).format(calendar.getTime()));
            }
        };

        // Edit text date click listener
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open date picker dialog
                new DatePickerDialog(
                        AddTaskActivity.this,
                        dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                ).show();
            }
        });

        // Edit text time click listener
        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open time picker dialog
                new TimePickerDialog(
                        AddTaskActivity.this,
                        timeSetListener,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                ).show();
            }
        });

        // Button add task click listener
        Button buttonAddTask = findViewById(R.id.buttonAddTask);
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAddTask();
            }
        });

        // Button add task type click listener
        ImageButton imageButtonAddTaskType = findViewById(R.id.imageButtonAddTaskType);
        imageButtonAddTaskType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(AddTaskActivity.this, AddTaskTypeActivity.class), REQUEST_ADD_TASK_TYPE);
            }
        });

        // Button edit task type click listener
        ImageButton imageButtonEditTaskType = findViewById(R.id.imageButtonEditTaskType);
        imageButtonEditTaskType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtain selected task type
                TaskType taskType = (TaskType) spinnerType.getSelectedItem();

                if (taskType == null) {
                    return;
                }

                // Create intent with task type as extra
                Intent intent = new Intent(AddTaskActivity.this, EditTaskTypeActivity.class);
                intent.putExtra(EditTaskTypeActivity.EXTRA_TASK_TYPE, taskType);
                startActivityForResult(intent, REQUEST_EDIT_TASK_TYPE);
            }
        });

        // Button remove task type click listener
        ImageButton imageButtonRemoveTaskType = findViewById(R.id.imageButtonRemoveTaskType);
        imageButtonRemoveTaskType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtain selected task type
                final TaskType taskType = (TaskType) spinnerType.getSelectedItem();

                if (taskType == null) {
                    return;
                }

                // Display confirm dialog
                new AlertDialog.Builder(AddTaskActivity.this)
                        .setTitle(R.string.action_remove_task_type)
                        .setMessage(R.string.dialog_confirm_remove_task_type)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Cancel task reminders of deleted task type
                                List<Task> taskList = db.getTasksByType(UserSession.getUser(), taskType);
                                for (Task task : taskList) {
                                    NotificationScheduler.cancelTaskReminder(AddTaskActivity.this, task);
                                }

                                boolean success = db.deleteTaskType(taskType);

                                if (success) {
                                    Toast.makeText(AddTaskActivity.this, R.string.success_remove_task_type, Toast.LENGTH_LONG).show();
                                    loadSpinnerData();
                                } else {
                                    // Set task reminders of specified task type in case type failed to delete
                                    taskList = db.getTasksByType(UserSession.getUser(), taskType);
                                    for (Task task : taskList) {
                                        NotificationScheduler.setTaskReminder(AddTaskActivity.this, task);
                                    }

                                    Snackbar.make(viewForm, R.string.error_remove_task_type, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        // Initial load of spinner data
        loadSpinnerData();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Cancel add task task
        if (addTaskTask != null) {
            addTaskTask.cancel(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Handle Action Bar Up button
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_ADD_TASK_TYPE || requestCode == REQUEST_EDIT_TASK_TYPE) && resultCode == AppCompatActivity.RESULT_OK) {
            // Reload spinner data after task types being modified
            int itemPosition = spinnerType.getSelectedItemPosition();
            loadSpinnerData();
            spinnerType.setSelection(itemPosition);
        }
    }

    /**
     * Function to attempt to add task
     */
    private void attemptAddTask() {
        // Make sure add task task is not already running
        if (addTaskTask != null) {
            return;
        }

        // Reset errors
        textInputLayoutTitle.setError(null);
        textInputLayoutTitle.setErrorEnabled(false);
        textInputLayoutDate.setError(null);
        textInputLayoutDate.setErrorEnabled(false);
        textInputLayoutTime.setError(null);
        textInputLayoutTime.setErrorEnabled(false);
        textInputLayoutDescription.setError(null);
        textInputLayoutDescription.setErrorEnabled(false);

        // Store values at the time of the add task attempt
        String title = editTextTitle.getText().toString();
        TaskType type = (TaskType) spinnerType.getSelectedItem();
        String date = editTextDate.getText().toString();
        String time = editTextTime.getText().toString();
        String description = editTextDescription.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid title
        if (TextUtils.isEmpty(title)) {
            textInputLayoutTitle.setErrorEnabled(true);
            textInputLayoutTitle.setError(getString(R.string.error_field_required));
            focusView = editTextTitle;
            cancel = true;
        }

        // Check for a valid type
        if (type == null) {
            Snackbar.make(viewForm, R.string.error_create_task_type, Snackbar.LENGTH_LONG).show();
            focusView = spinnerType;
            cancel = true;
        }

        // Check for a valid date
        if (TextUtils.isEmpty(date)) {
            textInputLayoutDate.setErrorEnabled(true);
            textInputLayoutDate.setError(getString(R.string.error_task_type_required));
            focusView = editTextDate;
            cancel = true;
        }

        // Check for a valid time
        if (TextUtils.isEmpty(time)) {
            textInputLayoutTime.setErrorEnabled(true);
            textInputLayoutTime.setError(getString(R.string.error_field_required));
            focusView = editTextTime;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // Start add task AsyncTask
            Util.displayProgressBar(AddTaskActivity.this, viewForm, viewProgressBar, true);
            addTaskTask = new AddTaskTask(AddTaskActivity.this, title, type.getId(), date, time, description);
            addTaskTask.execute((Void) null);
        }
    }

    /**
     * Function to load the spinner data from database
     */
    private void loadSpinnerData() {
        // Spinner Drop down elements
        List<TaskType> taskTypeList = db.getAllTaskTypes(UserSession.getUser());

        // Creating adapter for spinner
        ArrayAdapter<TaskType> arrayAdapterSpinner = new ArrayAdapter<>(AddTaskActivity.this, android.R.layout.simple_spinner_item, taskTypeList);

        // Drop down layout style - list view with radio button
        arrayAdapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Attaching data adapter to spinner
        spinnerType.setAdapter(arrayAdapterSpinner);
    }

    /**
     * AsyncTask class to handle task add
     */
    private static class AddTaskTask extends AsyncTask<Void, Void, Boolean> {

        private final WeakReference<AddTaskActivity> activityReference;
        private final String title;
        private final long type;
        private final String date;
        private final String time;
        private final String description;

        /**
         * Constructor
         *
         * @param activity    parent activity
         * @param title       task title
         * @param type        task tpye
         * @param date        task date
         * @param time        task time
         * @param description task description
         */
        AddTaskTask(AddTaskActivity activity, String title, long type, String date, String time, String description) {
            this.activityReference = new WeakReference<>(activity);
            this.title = title;
            this.type = type;
            this.date = date;
            this.time = time;
            this.description = description;
        }

        @Override
        public Boolean doInBackground(Void... params) {
            // Obtain parent activity
            final AddTaskActivity activity = activityReference.get();
            if (activity == null) {
                cancel(false);
                return false;
            }

            // Convert date and time to timestamp
            Date dateFormat;
            Date timeFormat;
            Calendar calendar = Calendar.getInstance();
            try {
                dateFormat = DateFormat.getDateFormat(activity).parse(date);
                timeFormat = DateFormat.getTimeFormat(activity).parse(time);

                calendar.set(dateFormat.getYear() + 1900, dateFormat.getMonth(), dateFormat.getDate(), timeFormat.getHours(), timeFormat.getMinutes());
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Request database handler to add new task
            Task task = activity.db.addTask(new Task(UserSession.getUser().getId(), title, type, calendar.getTimeInMillis(), description));

            // Check if task has been created
            if (task == null) {
                return false;
            }

            // Set task reminder to the new task
            NotificationScheduler.setTaskReminder(activity, task);

            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            // Obtain parent activity
            final AddTaskActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.addTaskTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);

            if (success) {
                Toast.makeText(activity, R.string.success_create_task, Toast.LENGTH_LONG).show();
                activity.finish();
            } else {
                Snackbar.make(activity.viewForm, R.string.error_create_task, Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancelled() {
            super.onCancelled();

            // Obtain parent activity
            final AddTaskActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.addTaskTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);
        }
    }
}
