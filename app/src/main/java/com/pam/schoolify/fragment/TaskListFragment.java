package com.pam.schoolify.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.pam.schoolify.R;
import com.pam.schoolify.activity.AddTaskActivity;
import com.pam.schoolify.activity.TaskDetailActivity;
import com.pam.schoolify.util.SQLiteHelper;
import com.pam.schoolify.util.UserSession;
import com.pam.schoolify.util.database.Task;
import com.pam.schoolify.util.database.TaskType;

import java.util.Calendar;
import java.util.List;

/**
 * A fragment representing a list of Tasks. This fragment
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link TaskDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class TaskListFragment extends Fragment {

    public static final String EXTRA_SELECTED_DATE = "selected_date";
    /**
     * The fragment argument representing the calendar that this fragment
     * represents.
     */
    public static final String ARG_CALENDAR = "calendar";
    public static final String ARG_FUTURE = "future";
    Bundle arguments;
    private boolean futureTasks = false;
    // Database helper
    private SQLiteHelper db;
    private Calendar calendar;
    // UI references
    private RecyclerView recyclerView;
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskListFragment() {
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public boolean getTwoPane() {
        return mTwoPane;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = this.getActivity();
        assert activity != null;

        db = new SQLiteHelper(activity);

        arguments = getArguments();
        assert arguments != null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_task_list, container, false);

        FloatingActionButton floatingActionButtonAddTask = rootView.findViewById(R.id.floatingActionButtonAddTask);
        floatingActionButtonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (calendar == null) {
                    return;
                }

                Intent intent = new Intent(getContext(), AddTaskActivity.class);
                intent.putExtra(EXTRA_SELECTED_DATE, calendar.getTimeInMillis());
                startActivity(intent);
            }
        });

        if (rootView.findViewById(R.id.task_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if (mTwoPane) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) floatingActionButtonAddTask.getLayoutParams();
            layoutParams.gravity = Gravity.BOTTOM | Gravity.START;
            floatingActionButtonAddTask.setLayoutParams(layoutParams);
        }

        recyclerView = rootView.findViewById(R.id.task_list);
        assert recyclerView != null;
        setupRecyclerView(recyclerView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        setupRecyclerView(recyclerView);
    }

    public void setupRecyclerView(RecyclerView recyclerView) {
        if (arguments.containsKey(ARG_CALENDAR)) {
            // Load the content specified by the fragment arguments
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(arguments.getLong(ARG_CALENDAR));
        }

        if (arguments.containsKey(ARG_FUTURE) && arguments.getBoolean(ARG_FUTURE)) {
            futureTasks = true;
        }

        if (calendar != null) {
            List<Task> taskList;
            if (futureTasks) {
                taskList = db.getFutureTasks(UserSession.getUser(), calendar);
            } else {
                taskList = db.getTasksByDate(UserSession.getUser(), calendar);
            }

            recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, taskList, mTwoPane));
        }
    }

    public static class SimpleItemRecyclerViewAdapter extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final TaskListFragment mParentFragment;
        private final List<Task> mValues;
        private final boolean mTwoPane;
        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Task item = (Task) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putLong(TaskDetailFragment.ARG_ITEM_ID, item.getId());
                    TaskDetailFragment fragment = new TaskDetailFragment();
                    fragment.setArguments(arguments);
                    FragmentManager fragmentManager = mParentFragment.getFragmentManager();
                    if (fragmentManager != null) {
                        fragmentManager.beginTransaction()
                                .replace(R.id.task_detail_container, fragment)
                                .commit();
                    }
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, TaskDetailActivity.class);
                    intent.putExtra(TaskDetailFragment.ARG_ITEM_ID, item.getId());

                    context.startActivity(intent);
                }
            }
        };

        SimpleItemRecyclerViewAdapter(TaskListFragment parent, List<Task> items, boolean twoPane) {
            mValues = items;
            mParentFragment = parent;
            mTwoPane = twoPane;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            TaskType taskType = mParentFragment.db.getTaskType(mValues.get(position).getType());

            holder.mColorView.setBackgroundColor(taskType.getColor());
            holder.mTitleView.setText(mValues.get(position).getTitle());
            holder.mTypeView.setText(taskType.getType());

            if (mParentFragment.futureTasks) {
                String dateTimeText = DateFormat.getDateFormat(mParentFragment.getContext()).format(mValues.get(position).getDateTime()) + " " +
                        DateFormat.getTimeFormat(mParentFragment.getContext()).format(mValues.get(position).getDateTime());
                holder.mDateTimeView.setText(dateTimeText);
            } else {
                holder.mDateTimeView.setText(DateFormat.getTimeFormat(mParentFragment.getContext()).format(mValues.get(position).getDateTime()));
            }

            holder.itemView.setTag(mValues.get(position));
            holder.itemView.setOnClickListener(mOnClickListener);
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final View mColorView;
            final TextView mTitleView;
            final TextView mTypeView;
            final TextView mDateTimeView;

            ViewHolder(View view) {
                super(view);

                mColorView = view.findViewById(R.id.taskColor);
                mTitleView = view.findViewById(R.id.taskTitle);
                mTypeView = view.findViewById(R.id.taskType);
                mDateTimeView = view.findViewById(R.id.taskDateTime);
            }
        }
    }
}
