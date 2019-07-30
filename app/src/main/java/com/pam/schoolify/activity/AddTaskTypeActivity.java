package com.pam.schoolify.activity;

import android.app.Activity;
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
 * Activity to add new task types
 */
public class AddTaskTypeActivity extends AppCompatActivity {

    /**
     * Result codes for task type add
     */
    private static final int RESULT_SUCCESS = 0;
    private static final int RESULT_FAILED = -1;
    private static final int RESULT_TASK_TYPE_USED = -2;

    /**
     * AsyncTask add task type handler
     */
    private AddTaskTypeTask addTaskTypeTask = null;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task_type);

        // Show the Up button in the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialize database handler
        db = new SQLiteHelper(AddTaskTypeActivity.this);

        // Obtain UI references
        textInputLayoutType = findViewById(R.id.textInputLayoutType);
        editTextType = findViewById(R.id.editTextType);
        colorPickerView = findViewById(R.id.colorPickerView);
        viewProgressBar = findViewById(R.id.progressBarAddTaskType);
        viewForm = findViewById(R.id.formAddTaskType);

        // Set up the add task type form
        colorPickerView.setColor(0xffffffff);

        // Button add task type click listener
        Button buttonAddTaskType = findViewById(R.id.buttonAddTaskType);
        buttonAddTaskType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAddTaskType();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        // Cancel add task type task
        if (addTaskTypeTask != null) {
            addTaskTypeTask.cancel(false);
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
     * Function to attempt to add task type
     */
    private void attemptAddTaskType() {
        // Make sure add task type task is not already running
        if (addTaskTypeTask != null) {
            return;
        }

        // Reset errors
        textInputLayoutType.setError(null);
        textInputLayoutType.setErrorEnabled(false);

        // Store values at the time of the add task type attempt
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
            // Start add task type AsyncTask
            Util.displayProgressBar(AddTaskTypeActivity.this, viewForm, viewProgressBar, true);
            addTaskTypeTask = new AddTaskTypeTask(AddTaskTypeActivity.this, type, color);
            addTaskTypeTask.execute((Void) null);
        }
    }

    /**
     * AsyncTask class to handle task type add
     */
    private static class AddTaskTypeTask extends AsyncTask<Void, Void, Integer> {

        private final WeakReference<AddTaskTypeActivity> activityReference;
        private final String type;
        private final String color;

        /**
         * Constructor
         *
         * @param activity parent activity
         * @param type     type name
         * @param color    type color
         */
        AddTaskTypeTask(AddTaskTypeActivity activity, String type, String color) {
            this.activityReference = new WeakReference<>(activity);
            this.type = type;
            this.color = color;
        }

        @Override
        public Integer doInBackground(Void... params) {
            // Obtain parent activity
            final AddTaskTypeActivity activity = activityReference.get();
            if (activity == null) {
                cancel(false);
                return RESULT_FAILED;
            }

            // Check if type name already exists
            if (activity.db.isTaskTypeUsed(UserSession.getUser(), type)) {
                return RESULT_TASK_TYPE_USED;
            }

            // Request database handler to add new task type
            TaskType taskType = activity.db.addTaskType(new TaskType(UserSession.getUser().getId(), type, color));

            // Check if task type has been created
            if (taskType == null) {
                return RESULT_FAILED;
            }

            return RESULT_SUCCESS;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);

            // Obtain parent activity
            final AddTaskTypeActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.addTaskTypeTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);

            switch (result) {
                case RESULT_SUCCESS:
                    Toast.makeText(activity, R.string.success_create_task_type, Toast.LENGTH_LONG).show();
                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                    break;
                case RESULT_FAILED:
                    Snackbar.make(activity.viewForm, R.string.error_create_task_type, Snackbar.LENGTH_LONG).show();
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
            final AddTaskTypeActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.addTaskTypeTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);
        }
    }
}
