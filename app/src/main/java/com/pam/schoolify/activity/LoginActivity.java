package com.pam.schoolify.activity;

import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.pam.schoolify.R;
import com.pam.schoolify.util.SQLiteHelper;
import com.pam.schoolify.util.UserSession;
import com.pam.schoolify.util.Util;
import com.pam.schoolify.util.database.User;

import java.lang.ref.WeakReference;

/**
 * Activity to login user
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * AsyncTask login handler
     */
    private LoginTask loginTask = null;

    /**
     * Database handler
     */
    private SQLiteHelper db;

    /**
     * UI references
     */
    private TextInputLayout textInputLayoutEmail;
    private TextInputEditText editTextEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextPassword;
    private View viewForm;
    private View viewProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database handler
        db = new SQLiteHelper(LoginActivity.this);

        // Obtain UI references
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        editTextEmail = findViewById(R.id.editTextEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        editTextPassword = findViewById(R.id.editTextPassword);
        viewProgressBar = findViewById(R.id.progressBarLogin);
        viewForm = findViewById(R.id.formLogin);

        // Edit text password attempt to login pressing Enter
        editTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // Button login click listener
        Button buttonLogin = findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        // Button register click listener
        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        // Cancel login task
        if (loginTask != null) {
            loginTask.cancel(false);
        }
    }

    /**
     * Function to attempt to login user
     */
    private void attemptLogin() {
        // Make sure login task is not already running
        if (loginTask != null) {
            return;
        }

        // Reset errors
        textInputLayoutEmail.setError(null);
        textInputLayoutEmail.setErrorEnabled(false);
        textInputLayoutPassword.setError(null);
        textInputLayoutPassword.setErrorEnabled(false);

        // Store values at the time of the login attempt
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            textInputLayoutPassword.setErrorEnabled(true);
            textInputLayoutPassword.setError(getString(R.string.error_field_required));
            focusView = editTextPassword;
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
            // Start login AsyncTask
            Util.displayProgressBar(LoginActivity.this, viewForm, viewProgressBar, true);
            loginTask = new LoginTask(LoginActivity.this, email, password);
            loginTask.execute((Void) null);
        }
    }

    /**
     * AsyncTask class to handle login
     */
    private static class LoginTask extends AsyncTask<Void, Void, Boolean> {

        private final WeakReference<LoginActivity> activityReference;
        private final String email;
        private final String password;

        /**
         * Constructor
         *
         * @param activity parent activity
         * @param email    user email address
         * @param password user password
         */
        LoginTask(LoginActivity activity, String email, String password) {
            this.activityReference = new WeakReference<>(activity);
            this.email = email;
            this.password = password;
        }

        @Override
        public Boolean doInBackground(Void... params) {
            // Obtain parent activity
            final LoginActivity activity = activityReference.get();
            if (activity == null) {
                cancel(false);
                return false;
            }

            // Request database handler to authenticate user
            User user = activity.db.authenticate(email, password);

            // Check if user has been authenticated
            if (user == null) {
                return false;
            }

            // create user session
            UserSession.createUserSession(activity, user);

            return true;
        }

        @Override
        public void onPostExecute(final Boolean success) {
            super.onPostExecute(success);

            // Obtain parent activity
            final LoginActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.loginTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);

            if (success) {
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
            } else {
                Snackbar.make(activity.viewForm, R.string.error_incorrect_credentials, Snackbar.LENGTH_LONG).show();

                // Clear password field at failed login
                activity.editTextPassword.setText("");
            }
        }

        @Override
        public void onCancelled() {
            super.onCancelled();

            // Obtain parent activity
            final LoginActivity activity = activityReference.get();
            if (activity == null) {
                return;
            }

            // AsyncTask end
            activity.loginTask = null;
            Util.displayProgressBar(activity, activity.viewForm, activity.viewProgressBar, false);
        }
    }
}
