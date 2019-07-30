package com.pam.schoolify.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pam.schoolify.R;
import com.pam.schoolify.util.SQLiteHelper;
import com.pam.schoolify.util.Util;
import com.pam.schoolify.util.database.User;

import java.lang.ref.WeakReference;

/**
 * Activity to register user
 */
public class RegisterActivity extends AppCompatActivity {

    /**
     * Result codes for task type add
     */
    private static final int RESULT_SUCCESS = 0;
    private static final int RESULT_FAILED = -1;
    private static final int RESULT_EMAIL_USED = -2;

    /**
     * AsyncTask register handler
     */
    private RegisterTask registerTask = null;

    /**
     * Database handler
     */
    private SQLiteHelper db;

    /**
     * UI references
     */
    private TextInputLayout textInputLayoutEmail;
    private TextInputEditText editTextEmail;
    private TextInputLayout textInputLayoutName;
    private TextInputEditText editTextName;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextPassword;
    private TextInputLayout textInputLayoutConfirmPassword;
    private TextInputEditText editTextConfirmPassword;
    private View viewForm;
    private View viewProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize database handler
        db = new SQLiteHelper(RegisterActivity.this);

        // Obtain UI references
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        editTextEmail = findViewById(R.id.editTextEmail);
        textInputLayoutName = findViewById(R.id.textInputLayoutName);
        editTextName = findViewById(R.id.editTextName);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        editTextPassword = findViewById(R.id.editTextPassword);
        textInputLayoutConfirmPassword = findViewById(R.id.textInputLayoutConfirmPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        viewProgressBar = findViewById(R.id.progressBarRegister);
        viewForm = findViewById(R.id.formRegister);

        // Edit text confirm password atrempt to register pressing Enter
        editTextConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        // Button register click listener
        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        // Cancel register task
        if (registerTask != null) {
            registerTask.cancel(false);
        }
    }

    /**
     * Function to attempt to register user
     */
    private void attemptRegister() {
        // Make sure register task is not already running
        if (registerTask != null) {
            return;
        }

        // Reset errors
        textInputLayoutEmail.setError(null);
        textInputLayoutEmail.setErrorEnabled(false);
        textInputLayoutName.setError(null);
        textInputLayoutName.setErrorEnabled(false);
        textInputLayoutPassword.setError(null);
        textInputLayoutPassword.setErrorEnabled(false);
        textInputLayoutConfirmPassword.setError(null);
        textInputLayoutConfirmPassword.setErrorEnabled(false);

        // Store values at the time of the register attempt
        String email = editTextEmail.getText().toString();
        String name = editTextName.getText().toString();
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid confirm password
        if (!confirmPassword.equals(password)) {
            textInputLayoutConfirmPassword.setErrorEnabled(true);
            textInputLayoutConfirmPassword.setError(getString(R.string.error_invalid_confirm_password));
            focusView = editTextConfirmPassword;
            cancel = true;
        }

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            textInputLayoutPassword.setErrorEnabled(true);
            textInputLayoutPassword.setError(getString(R.string.error_field_required));
            focusView = editTextPassword;
            cancel = true;
        } else if (!User.isPasswordValid(password)) {
            textInputLayoutPassword.setErrorEnabled(true);
            textInputLayoutPassword.setError(getString(R.string.error_invalid_password));
            focusView = editTextPassword;
            cancel = true;
        }

        // Check for a valid name
        if (TextUtils.isEmpty(name)) {
            textInputLayoutName.setErrorEnabled(true);
            textInputLayoutName.setError(getString(R.string.error_field_required));
            focusView = editTextName;
            cancel = true;
        }

        // Check for a valid email address
        if (TextUtils.isEmpty(email)) {
            textInputLayoutEmail.setErrorEnabled(true);
            textInputLayoutEmail.setError(getString(R.string.error_field_required));
            focusView = editTextEmail;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.setErrorEnabled(true);
            textInputLayoutEmail.setError(getString(R.string.error_invalid_email));
            focusView = editTextEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            // Start register AsyncTask
            Util.displayProgressBar(RegisterActivity.this, viewForm, viewProgressBar, true);
            registerTask = new RegisterTask(RegisterActivity.this, email, name, password);
            registerTask.execute((Void) null);
        }
    }

    /**
     * AsyncTask class to handle registration
     */
    private static class RegisterTask extends AsyncTask<Void, Void, Integer> {

        private final WeakReference<RegisterActivity> activityReference;
        private final String email;
        private final String name;
        private final String password;

        /**
         * Constructor
         *
         * @param activity parent activity
         * @param email    user email
         * @param name     user name
         * @param password user password
         */
        RegisterTask(RegisterActivity activity, String email, String name, String password) {
            this.activityReference = new WeakReference<>(activity);
            this.email = email;
            this.name = name;
            this.password = password;
        }

        @Override
        public Integer doInBackground(Void... params) {
            // Obtain parent activity
            final RegisterActivity activity = activityReference.get();
            if (activity == null) {
                cancel(false);
                return RESULT_FAILED;
            }

            // Check if email already exists
            if (activity.db.isEmailUsed(email)) {
                return RESULT_EMAIL_USED;
            }

            // Request database handler to register user
            User user = activity.db.addUser(new User(email, name, password));

            // Check if user has been registered
            if (user == null) {
                return RESULT_FAILED;
            }

            return RESULT_SUCCESS;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);

            // Obtain parent activity
            final RegisterActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.registerTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);

            switch (result) {
                case RESULT_SUCCESS:
                    Toast.makeText(activity, R.string.success_create_user, Toast.LENGTH_LONG).show();
                    activity.finish();
                    break;
                case RESULT_FAILED:
                    Snackbar.make(activity.viewForm, R.string.error_create_user, Snackbar.LENGTH_LONG).show();
                    break;
                case RESULT_EMAIL_USED:
                    activity.textInputLayoutEmail.setErrorEnabled(true);
                    activity.textInputLayoutEmail.setError(activity.getString(R.string.error_same_email));
                    activity.editTextEmail.requestFocus();
                    break;
            }
        }

        @Override
        public void onCancelled() {
            super.onCancelled();

            // Obtain parent activity
            final RegisterActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.registerTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);
        }
    }
}
