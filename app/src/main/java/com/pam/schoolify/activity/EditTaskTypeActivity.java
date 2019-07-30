package com.pam.schoolify.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.pam.schoolify.R;
import com.pam.schoolify.util.SQLiteHelper;
import com.pam.schoolify.util.UserSession;
import com.pam.schoolify.util.Util;
import com.pam.schoolify.util.database.TaskType;
import com.rarepebble.colorpicker.ColorPickerView;

import java.lang.ref.WeakReference;

/**
 * Activity to edit task types
 */
public class EditTaskTypeActivity extends AppCompatActivity {

    /**
     * Intent extras strings
     */
    public static final String EXTRA_TASK_TYPE = "task_type";

    /**
     * Result codes for task type update
     */
    private static final int RESULT_SUCCESS = 0;
    private static final int RESULT_FAILED = -1;
    private static final int RESULT_TASK_TYPE_USED = -2;

    /**
     * AsyncTask edit task type handler
     */
    private EditTaskTypeTask editTaskTypeTask = null;

    /**
     * Database handler
     */
    private SQLiteHelper db;

    /**
     * UI references
     */
    private TextInputLayout textInputLayoutType;
    private TextInputEditText editTextType;
    private ColorPickerView colorPickerView;
    private View viewForm;
    private View viewProgressBar;

    /**
     * Task type passed by parent
     */
    private TaskType taskType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task_type);

        // Show the Up button in the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialize database handler
        db = new SQLiteHelper(EditTaskTypeActivity.this);

        // Obtain UI references
        textInputLayoutType = findViewById(R.id.textInputLayoutType);
        editTextType = findViewById(R.id.editTextType);
        colorPickerView = findViewById(R.id.colorPickerView);
        viewProgressBar = findViewById(R.id.progressBarEditTaskType);
        viewForm = findViewById(R.id.formEditTaskType);

        // Button edit task type click listener
        Button buttonEditTaskType = findViewById(R.id.buttonEditTaskType);
        buttonEditTaskType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptEditTaskType();
            }
        });

        // Load passed task type data
        loadTaskTypeData();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Cancel edit task type task
        if (editTaskTypeTask != null) {
            editTaskTypeTask.cancel(false);
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

    /**
     * Function to attempt to edit task type
     */
    private void attemptEditTaskType() {
        // Make sure edit task type task is not already running
        if (editTaskTypeTask != null) {
            return;
        }

        // Reset errors
        textInputLayoutType.setError(null);
        textInputLayoutType.setErrorEnabled(false);

        // Store values at the time of the edit task type attempt
        String type = editTextType.getText().toString();
        String color = Integer.toString(colorPickerView.getColor());

        boolean cancel = false;
        View focusView = null;

        // Check for a valid task type
        if (TextUtils.isEmpty(type)) {
            textInputLayoutType.setErrorEnabled(true);
            textInputLayoutType.setError(getString(R.string.error_field_required));
            focusView = editTextType;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // Start edit task type AsyncTask
            Util.displayProgressBar(EditTaskTypeActivity.this, viewForm, viewProgressBar, true);
            editTaskTypeTask = new EditTaskTypeTask(EditTaskTypeActivity.this, type, color);
            editTaskTypeTask.execute((Void) null);
        }
    }

    /**
     * Function to load the data from intent extra
     */
    private void loadTaskTypeData() {
        // Get activity intent
        Intent intent = getIntent();

        // Check if extras have been passed
        if (intent.getExtras() != null) {
            // Get parcelable task type extra
            taskType = intent.getParcelableExtra(EXTRA_TASK_TYPE);

            // Set UI data to the passed task type data
            editTextType.setText(taskType.getType());
            colorPickerView.setColor(taskType.getColor());
        }
    }

    /**
     * AsyncTask class to handle task type edit
     */
    private static class EditTaskTypeTask extends AsyncTask<Void, Void, Integer> {

        private final WeakReference<EditTaskTypeActivity> activityReference;
        private final String type;
        private final String color;

        /**
         * Constructor
         *
         * @param activity parent activity
         * @param type     new type name
         * @param color    new type color
         */
        EditTaskTypeTask(EditTaskTypeActivity activity, String type, String color) {
            this.activityReference = new WeakReference<>(activity);
            this.type = type;
            this.color = color;
        }

        @Override
        public Integer doInBackground(Void... params) {
            // Obtain parent activity
            final EditTaskTypeActivity activity = activityReference.get();
            if (activity == null) {
                cancel(false);
                return RESULT_FAILED;
            }

            // Check if type name already exists (only if it is not the same)
            if (!type.equals(activity.taskType.getType()) && activity.db.isTaskTypeUsed(UserSession.getUser(), type)) {
                return RESULT_TASK_TYPE_USED;
            }

            // Set new task type data
            activity.taskType.setType(type);
            activity.taskType.setColor(color);

            // Request database handler to update task type
            TaskType taskType = activity.db.updateTaskType(activity.taskType);

            // Check if task type has been updated
            if (taskType == null) {
                return RESULT_FAILED;
            }

            return RESULT_SUCCESS;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);

            // Obtain parent activity
            final EditTaskTypeActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.editTaskTypeTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);

            switch (result) {
                case RESULT_SUCCESS:
                    Toast.makeText(activity, R.string.success_edit_task_type, Toast.LENGTH_LONG).show();
                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                    break;
                case RESULT_FAILED:
                    Snackbar.make(activity.viewForm, R.string.error_edit_task_type, Snackbar.LENGTH_LONG).show();
                    break;
                case RESULT_TASK_TYPE_USED:
                    activity.textInputLayoutType.setErrorEnabled(true);
                    activity.textInputLayoutType.setError(activity.getString(R.string.error_same_task_type));
                    activity.editTextType.requestFocus();
                    break;
            }
        }

        @Override
        public void onCancelled() {
            super.onCancelled();

            // Obtain parent activity
            final EditTaskTypeActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.editTaskTypeTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);
        }
    }
}
