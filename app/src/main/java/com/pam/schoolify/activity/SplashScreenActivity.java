package com.pam.schoolify.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pam.schoolify.R;
import com.pam.schoolify.util.UserSession;

public class SplashScreenActivity extends Activity {

    /**
     * Splash screen display time in milliseconds
     */
    private static final int SPLASH_TIME_OUT = 3000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Execute after time out
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if there is a valid user session
                if (UserSession.isUserSessionValid(SplashScreenActivity.this)) {
                    // If user session valid send to MainActivity
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                } else {
                    // If no user session send to LoginActivity
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                }

                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
