package com.pam.schoolify.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.pam.schoolify.R;
import com.pam.schoolify.fragment.TaskDetailFragment;

/**
 * An activity representing a single Task detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link TaskListActivity}.
 */
public class TaskDetailActivity extends AppCompatActivity implements TaskDetailFragment.OnFragmentInteractionListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Get if the activity has been started by a notification click listener
        boolean isNotification = getIntent().getBooleanExtra(TaskDetailFragment.ARG_NOTIFICTION, false);

        // If not started by a notification
        if (!isNotification) {
            // Show the Up button in the action bar
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(TaskDetailFragment.ARG_ITEM_ID,
                    getIntent().getLongExtra(TaskDetailFragment.ARG_ITEM_ID, -1));
            arguments.putBoolean(TaskDetailFragment.ARG_NOTIFICTION, isNotification);
            TaskDetailFragment fragment = new TaskDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.task_detail_container, fragment)
                    .commitNow();
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
     * Event fired when task a is removed
     */
    @Override
    public void onTaskRemoved() {
        // Just finish current activity
        finish();
    }
}
