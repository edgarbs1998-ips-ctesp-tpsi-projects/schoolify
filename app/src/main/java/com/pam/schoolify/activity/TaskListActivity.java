package com.pam.schoolify.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.MenuItem;

import com.pam.schoolify.R;
import com.pam.schoolify.fragment.TaskDetailFragment;
import com.pam.schoolify.fragment.TaskListFragment;

import java.util.Calendar;

/**
 * An activity representing a list of Tasks. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TaskDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class TaskListActivity extends AppCompatActivity implements TaskDetailFragment.OnFragmentInteractionListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        // Show the Up button in the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Initialize calendar
        Calendar calendar = Calendar.getInstance();

        // Get activity intent
        Intent intent = getIntent();

        // Declare and initialize variable selectedDateInMillis
        long selectedDateInMillis = System.currentTimeMillis();

        // Check if extras have been passed
        if (intent.getExtras() != null) {
            // Get CalendarFragment clicked date
            selectedDateInMillis = intent.getLongExtra(TaskListFragment.EXTRA_SELECTED_DATE, System.currentTimeMillis());
            calendar.setTimeInMillis(selectedDateInMillis);
        }

        // Set activity title based on current date
        setTitle(getTitle() + " - " + DateFormat.getDateFormat(TaskListActivity.this).format(calendar.getTime()));

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
            // Create the list fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putLong(TaskListFragment.ARG_CALENDAR, selectedDateInMillis);
            arguments.putBoolean(TaskListFragment.ARG_FUTURE, false);
            TaskListFragment fragment = new TaskListFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.task_container, fragment)
                    .commit();
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
     * Event fired when a task is removed
     */
    @Override
    public void onTaskRemoved() {
        // Obtain task list fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        TaskListFragment taskListFragment = (TaskListFragment) fragmentManager.findFragmentById(R.id.task_container);

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
