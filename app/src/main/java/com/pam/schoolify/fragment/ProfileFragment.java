package com.pam.schoolify.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.pam.schoolify.R;
import com.pam.schoolify.util.Gravatar;
import com.pam.schoolify.util.SQLiteHelper;
import com.pam.schoolify.util.UserSession;
import com.pam.schoolify.util.Util;
import com.pam.schoolify.util.database.User;

import java.lang.ref.WeakReference;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Profile fragment displayed on MainActivity
 */
public class ProfileFragment extends Fragment {

    private static final int RESULT_SUCCESS = 0;
    private static final int RESULT_FAILED = -1;
    private static final int RESULT_EMAIL_USED = -2;

    private UpdateProfileTask updateProfileTask = null;

    // Database helper
    private SQLiteHelper db;

    // UI references
    private ImageView imageViewProfileImage;
    private TextInputLayout textInputLayoutEmail;
    private TextInputEditText editTextEmail;
    private TextInputLayout textInputLayoutName;
    private TextInputEditText editTextName;
    private View viewForm;
    private View viewProgressBar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProfileFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = new SQLiteHelper(view.getContext());

        imageViewProfileImage = view.findViewById(R.id.imageViewProfileImage);
        textInputLayoutEmail = view.findViewById(R.id.textInputLayoutEmail);
        editTextEmail = view.findViewById(R.id.editTextEmail);
        textInputLayoutName = view.findViewById(R.id.textInputLayoutName);
        editTextName = view.findViewById(R.id.editTextName);
        viewForm = view.findViewById(R.id.formProfile);
        viewProgressBar = view.findViewById(R.id.progressBarProfile);

        imageViewProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://pt.gravatar.com/emails/")));
            }
        });

        Button buttonUpdateProfile = view.findViewById(R.id.buttonUpdateProfile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptUpdateProfile();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadProfileData();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (updateProfileTask != null) {
            updateProfileTask.cancel(false);
        }
    }

    private void loadProfileData() {
        User user = UserSession.getUser();

        editTextEmail.setText(user.getEmail());
        editTextName.setText(user.getName());

        String avatarUrl = new Gravatar()
                .setSize(200)
                .setDefaultImage(Gravatar.GravatarDefaultImage.IDENTICON)
                .getUrl(user.getEmail());

        Glide.with(this)
                .load(avatarUrl)
                .transition(withCrossFade())
                .apply(
                        new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .circleCrop())
                .into(imageViewProfileImage);
    }

    private void attemptUpdateProfile() {
        if (updateProfileTask != null) {
            return;
        }

        // Reset errors
        textInputLayoutEmail.setError(null);
        textInputLayoutEmail.setErrorEnabled(false);
        textInputLayoutName.setError(null);
        textInputLayoutName.setErrorEnabled(false);

        // Store values at the time of the update profile attempt
        String email = editTextEmail.getText().toString();
        String name = editTextName.getText().toString();

        boolean cancel = false;
        View focusView = null;

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
            Util.displayProgressBar(getContext(), viewForm, viewProgressBar, true);
            updateProfileTask = new UpdateProfileTask(ProfileFragment.this, email, name);
            updateProfileTask.execute((Void) null);
        }
    }

    private static class UpdateProfileTask extends AsyncTask<Void, Void, Integer> {

        private final WeakReference<ProfileFragment> fragmentReference;
        private final String email;
        private final String name;

        UpdateProfileTask(ProfileFragment fragment, String email, String name) {
            this.fragmentReference = new WeakReference<>(fragment);
            this.email = email;
            this.name = name;
        }

        @Override
        public Integer doInBackground(Void... params) {
            final ProfileFragment fragment = fragmentReference.get();
            if (fragment == null) {
                cancel(false);
                return RESULT_FAILED;
            }

            User user = UserSession.getUser();

            if (!email.equals(user.getEmail()) && fragment.db.isEmailUsed(email)) {
                return RESULT_EMAIL_USED;
            }

            user.setEmail(email);
            user.setName(name);

            User updateUser = fragment.db.updateUser(user);

            if (updateUser == null) {
                return RESULT_FAILED;
            }

            UserSession.destroyUserSession(fragment.getContext());
            UserSession.createUserSession(fragment.getContext(), updateUser);

            return RESULT_SUCCESS;
        }

        @Override
        public void onPostExecute(Integer result) {
            super.onPostExecute(result);

            final ProfileFragment fragment = fragmentReference.get();
            if (fragment == null) {
                return;
            }

            fragment.updateProfileTask = null;
            Util.displayProgressBar(fragment.getContext(), fragment.viewForm, fragment.viewProgressBar, false);

            switch (result) {
                case RESULT_SUCCESS:
                    Snackbar.make(fragment.viewForm, R.string.success_update_profile, Snackbar.LENGTH_LONG).show();
                    fragment.loadProfileData();
                    break;
                case RESULT_FAILED:
                    Snackbar.make(fragment.viewForm, R.string.error_update_profile, Snackbar.LENGTH_LONG).show();
                    break;
                case RESULT_EMAIL_USED:
                    fragment.textInputLayoutEmail.setErrorEnabled(true);
                    fragment.textInputLayoutEmail.setError(fragment.getString(R.string.error_same_email));
                    fragment.editTextEmail.requestFocus();
                    break;
            }
        }

        @Override
        public void onCancelled() {
            super.onCancelled();

            final ProfileFragment fragment = fragmentReference.get();
            if (fragment == null) {
                return;
            }

            fragment.updateProfileTask = null;
            Util.displayProgressBar(fragment.getContext(), fragment.viewForm, fragment.viewProgressBar, false);
        }
    }
}
