package com.pam.schoolify.activity;

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
import com.pam.schoolify.util.database.User;

import java.lang.ref.WeakReference;

/**
 * Activity to change user password
 */
public class ChangePasswordActivity extends AppCompatActivity {

    /**
     * AsyncTask change user password handler
     */
    private ChangePasswordTask changePasswordTask = null;

    /**
     * Database handler
     */
    private SQLiteHelper db;

    /**
     * UI references
     */
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextPassword;
    private TextInputLayout textInputLayoutConfirmPassword;
    private TextInputEditText editTextConfirmPassword;
    private View viewForm;
    private View viewProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Show the Up button in the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialize database handler
        db = new SQLiteHelper(ChangePasswordActivity.this);

        // Obtain UI references
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        editTextPassword = findViewById(R.id.editTextPassword);
        textInputLayoutConfirmPassword = findViewById(R.id.textInputLayoutConfirmPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        viewForm = findViewById(R.id.formChangePassword);
        viewProgressBar = findViewById(R.id.progressBarChangePassword);

        // Button change password click listener
        Button buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptChangePassword();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        // Cancel change user password
        if (changePasswordTask != null) {
            changePasswordTask.cancel(false);
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
     * Function to attempt to change user password
     */
    private void attemptChangePassword() {
        // Make sure change user password task is not already running
        if (changePasswordTask != null) {
            return;
        }

        // Reset errors
        textInputLayoutPassword.setError(null);
        textInputLayoutPassword.setErrorEnabled(false);
        textInputLayoutConfirmPassword.setError(null);
        textInputLayoutConfirmPassword.setErrorEnabled(false);

        // Store values at the time of the change user password attempt
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

        if (cancel) {
            focusView.requestFocus();
        } else {
            // Start change user password AsyncTask
            Util.displayProgressBar(ChangePasswordActivity.this, viewForm, viewProgressBar, true);
            changePasswordTask = new ChangePasswordTask(ChangePasswordActivity.this, password);
            changePasswordTask.execute((Void) null);
        }
    }

    /**
     * AsyncTask class to handle change user password
     */
    private static class ChangePasswordTask extends AsyncTask<Void, Void, Boolean> {

        private final WeakReference<ChangePasswordActivity> activityReference;
        private final String password;

        /**
         * Constructor
         *
         * @param activity parent activity
         * @param password new user password
         */
        ChangePasswordTask(ChangePasswordActivity activity, String password) {
            this.activityReference = new WeakReference<>(activity);
            this.password = password;
        }

        @Override
        public Boolean doInBackground(Void... params) {
            // Obtain parent activity
            final ChangePasswordActivity activity = activityReference.get();
            if (activity == null) {
                cancel(false);
                return false;
            }

            // Obtain current user and update the password
            User user = UserSession.getUser();
            user.setPassword(password);

            // Request database handler to change user password
            User updateUser = activity.db.changePassword(user);

            // Check if user password has been changed
            if (updateUser == null) {
                return false;
            }

            // Update user session
            UserSession.destroyUserSession(activity);
            UserSession.createUserSession(activity, user);

            return true;
        }

        @Override
        public void onPostExecute(Boolean success) {
            super.onPostExecute(success);

            // Obtain parent activity
            final ChangePasswordActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.changePasswordTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);

            if (success) {
                Toast.makeText(activity, R.string.success_change_password, Toast.LENGTH_LONG).show();
                activity.finish();
            } else {
                Snackbar.make(activity.viewForm, R.string.error_change_password, Snackbar.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancelled() {
            super.onCancelled();

            // Obtain parent activity
            final ChangePasswordActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.changePasswordTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);
        }
    }
}
