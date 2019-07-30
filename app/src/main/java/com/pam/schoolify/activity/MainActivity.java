package com.pam.schoolify.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.pam.schoolify.R;
import com.pam.schoolify.fragment.CalendarFragment;
import com.pam.schoolify.fragment.ProfileFragment;
import com.pam.schoolify.fragment.TaskDetailFragment;
import com.pam.schoolify.fragment.TaskListFragment;
import com.pam.schoolify.util.Gravatar;
import com.pam.schoolify.util.Gravatar.GravatarDefaultImage;
import com.pam.schoolify.util.SQLiteHelper;
import com.pam.schoolify.util.UserSession;
import com.pam.schoolify.util.database.User;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Main activity which handles menu drawer and fragments
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TaskDetailFragment.OnFragmentInteractionListener {

    /**
     * Fragments tags
     */
    private static final String TAG_CALENDER = "calender";
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_FUTURE_TASKS = "future_tasks";
    private static String currentTag;

    /**
     * General handler
     */
    private Handler handler;

    /**
     * Database handler
     */
    private SQLiteHelper db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize general handler
        handler = new Handler();

        // Initialize database handler
        db = new SQLiteHelper(MainActivity.this);

        // Setup navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            // Called when a drawer has settled in a completely closed state
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            // Called when a drawer has settled in a completely open state
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                loadNavigationHeader();
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation view
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // If no savedInstanceState load the main fragment
        if (savedInstanceState == null) {
            loadFragment(R.id.navCalender, TAG_CALENDER);
        }
    }

    /**
     * Load navigatin menu header data
     */
    private void loadNavigationHeader() {
        // Obtain UI references
        NavigationView navigationView = findViewById(R.id.nav_view);
        View navigationHeader = navigationView.getHeaderView(0);
        ImageView imageViewProfileImage = navigationHeader.findViewById(R.id.imageViewProfileImage);
        TextView textViewProfileName = navigationHeader.findViewById(R.id.textViewProfileName);
        TextView textViewProfileEmail = navigationHeader.findViewById(R.id.textViewProfileEmail);

        // Get user from session
        User user = UserSession.getUser();

        // Set header data
        textViewProfileName.setText(user.getName());
        textViewProfileEmail.setText(user.getEmail());

        // Obtain avatar URL from Gravatar
        String avatarUrl = new Gravatar()
                .setSize(75)
                .setDefaultImage(GravatarDefaultImage.IDENTICON)
                .getUrl(user.getEmail());

        // Load and display gravatar image with Glide library
        Glide.with(this)
                .load(avatarUrl)
                .transition(withCrossFade())
                .apply(
                        new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .circleCrop())
                .into(imageViewProfileImage);
    }


    /**
     * Set toolbar title
     *
     * @param title new toolbar title
     */
    private void setToolbarTitle(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    /**
     * Get Fragment my it's id
     *
     * @param id fragment id
     * @return {@link Fragment}
     */
    private Fragment getFragment(int id) {
        switch (id) {
            case R.id.navProfile:
                currentTag = TAG_PROFILE;
                return new ProfileFragment();
            case R.id.navFutureTasks:
                currentTag = TAG_FUTURE_TASKS;
                return new TaskListFragment();
            default:
                currentTag = TAG_CALENDER;
                return new CalendarFragment();
        }
    }

    /**
     * Loads fragments with default title
     *
     * @param id  fragment id
     * @param tag fragment tag
     */
    private void loadFragment(final int id, final String tag) {
        loadFragment(id, tag, null);
    }

    /**
     * Load fragments
     *
     * @param id    fragment id
     * @param tag   fragment tag
     * @param title fragment title
     */
    private void loadFragment(final int id, final String tag, CharSequence title) {
        // Check if title is provided
        if (title == null) {
            // Set default title
            title = getString(R.string.app_name);
        }
        setToolbarTitle(title);

        // Stop load fragment if request fragment is the same as the one currently loaded
        if (getSupportFragmentManager().findFragmentByTag(tag) != null) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        // Obtain and replace fragment
        final Fragment fragment = getFragment(id);
        Runnable pendingRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

                // Add extra arguments if loaded fragment is Future Tasks
                if (currentTag.equals(TAG_FUTURE_TASKS)) {
                    Bundle arguments = new Bundle();
                    arguments.putLong(TaskListFragment.ARG_CALENDAR, System.currentTimeMillis());
                    arguments.putBoolean(TaskListFragment.ARG_FUTURE, true);
                    fragment.setArguments(arguments);
                }

                fragmentTransaction.replace(R.id.fragmentContainer, fragment, currentTag);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // Add fragment replacement to the general handler
        handler.post(pendingRunnable);

        // Visual select loaded fragment on navigation menu
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(id).setChecked(true);

        // Force options menu to be recreated
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        // Close navigation menu if opened
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        }

        // Navigate to Calendar fragment
        if (!currentTag.equals(TAG_CALENDER)) {
            loadFragment(R.id.navCalender, TAG_CALENDER);
            return;
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // when fragment is user profile, load the menu created for user profile
        if (currentTag.equals(TAG_PROFILE)) {
            getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // user is in user profile fragment
        // and selected 'Change password'
        if (id == R.id.menuChangePassword) {
            startActivity(new Intent(MainActivity.this, ChangePasswordActivity.class));
        }

        // user is in user profile fragment
        // and selected 'Remove user'
        if (id == R.id.menuRemoveUser) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.action_remove_user)
                    .setMessage(R.string.dialog_confirm_remove_user)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Destroy usrer session
                            UserSession.destroyUserSession(MainActivity.this);

                            boolean success = db.deleteUser(UserSession.getUser());

                            if (success) {
                                Toast.makeText(MainActivity.this, R.string.success_remove_user, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, R.string.error_remove_user, Toast.LENGTH_LONG).show();
                            }

                            // Display LoginActivity
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                            finish();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Get selected item id and title
        int id = item.getItemId();
        CharSequence title = item.getTitle();

        // Load selected fragment or start corresponding activity
        switch (id) {
            case R.id.navProfile:
                loadFragment(id, TAG_PROFILE, title);
                break;
            case R.id.navFutureTasks:
                loadFragment(id, TAG_FUTURE_TASKS, title);
                break;
            case R.id.navAboutUs:
                startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
                break;
            case R.id.navSettings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.navLogout:
                // Confirm logout user
                new AlertDialog.Builder(this)
                        .setTitle(R.string.action_sign_out)
                        .setMessage(R.string.dialog_confirm_sign_out)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Destroy user session
                                UserSession.destroyUserSession(MainActivity.this);

                                // Start LoginActivity
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                break;
            default:
                loadFragment(id, TAG_CALENDER);
        }

        // Close navigation menu
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * Event fired when a task is removed
     */
    @Override
    public void onTaskRemoved() {
        // Obtain task list fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        TaskListFragment taskListFragment = (TaskListFragment) fragmentManager.findFragmentById(R.id.fragmentContainer);

        // Check if task list fragment is valid
        if (taskListFragment == null) {
            return;
        }

        // If fragment is in two pane mode
        if (taskListFragment.getTwoPane()) {
            // Remove task detail fragment
            fragmentManager.beginTransaction().remove(fragmentManager.findFragmentById(R.id.task_detail_container)).commit();

            // Refresh task list fragment
            taskListFragment.setupRecyclerView(taskListFragment.getRecyclerView());
        }
    }
}
