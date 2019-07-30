package com.pam.schoolify.util;

import android.content.Context;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {

    /**
     * Display form progress bar and hides the corresponding form
     *
     * @param context     context
     * @param form        form view
     * @param progressBar progress bar view
     * @param show        show or hide
     */
    public static void displayProgressBar(Context context, final View form, final View progressBar, final boolean show) {
        // Anim duration
        int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

        // Handle form visibility with animation
        form.setVisibility(show ? View.VISIBLE : View.GONE);
        AlphaAnimation formAnimation = new AlphaAnimation(show ? 1 : 0, show ? 0 : 1);
        formAnimation.setDuration(shortAnimTime);
        formAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                form.setVisibility(show ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        form.startAnimation(formAnimation);

        // Handle progress bar visibility with animation
        progressBar.setVisibility(show ? View.GONE : View.VISIBLE);
        AlphaAnimation progressBarAnimation = new AlphaAnimation(show ? 0 : 1, show ? 1 : 0);
        progressBarAnimation.setDuration(shortAnimTime);
        progressBarAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        progressBar.startAnimation(progressBarAnimation);
    }

    /**
     * Convert byte array to string
     *
     * @param array byte array
     * @return converted string
     */
    private static String byteArrayToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte anArray : array) {
            sb.append(Integer.toHexString((anArray
                    & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    /**
     * Hash message using sha-256 algorithm
     *
     * @param message message to be hashed
     * @return hashed message
     */
    public static String sha256Hex(String message) {
        String messageHash = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            messageHash = byteArrayToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return messageHash;
    }

    /**
     * Hash message using md5 algorithm
     *
     * @param message message to be hashed
     * @return hashed message
     */
    public static String md5Hex(String message) {
        String messageHash = null;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("CP1252"));
            messageHash = byteArrayToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return messageHash;
    }
}
