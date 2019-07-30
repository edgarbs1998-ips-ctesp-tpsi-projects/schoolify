package com.pam.schoolify.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.pam.schoolify.R;
import com.pam.schoolify.activity.EditTaskActivity;
import com.pam.schoolify.activity.TaskDetailActivity;
import com.pam.schoolify.activity.TaskListActivity;
import com.pam.schoolify.util.SQLiteHelper;
import com.pam.schoolify.util.database.Task;
import com.pam.schoolify.util.database.TaskType;

import java.util.Calendar;

/**
 * A fragment representing a single Task detail screen.
 * This fragment is either contained in a {@link TaskListActivity}
 * in two-pane mode (on tablets) or a {@link TaskDetailActivity}
 * on handsets.
 */
public class TaskDetailFragment extends Fragment {

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_NOTIFICTION = "notification";
    private static final int REQUEST_EDIT_TASK = 1;
    Bundle arguments;
    private OnFragmentInteractionListener mListener;
    // Database helper
    private SQLiteHelper db;
    // UI references
    private FloatingActionMenu floatingActionButtonMenu;
    /**
     * The content this fragment is presenting.
     */
    private Task mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TaskDetailFragment() {
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
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.task_detail, container, false);

        floatingActionButtonMenu = rootView.findViewById(R.id.floatingActionButtonMenu);

        FloatingActionButton floatingActionButtonEditTask = rootView.findViewById(R.id.floatingActionButtonEditTask);
        floatingActionButtonEditTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionButtonMenu.close(true);

                if (mItem == null) {
                    return;
                }

                Intent intent = new Intent(getActivity(), EditTaskActivity.class);
                intent.putExtra(EditTaskActivity.EXTRA_TASK, mItem);
                startActivityForResult(intent, REQUEST_EDIT_TASK);
            }
        });

        FloatingActionButton floatingActionButtonRemoveTask = rootView.findViewById(R.id.floatingActionButtonRemoveTask);
        floatingActionButtonRemoveTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionButtonMenu.close(true);

                if (mItem == null) {
                    return;
                }

                new AlertDialog.Builder(rootView.getContext())
                        .setTitle(R.string.action_remove_task)
                        .setMessage(R.string.dialog_confirm_remove_task)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean success = db.deleteTask(mItem);

                                if (success) {
                                    FragmentActivity fragmentActivity = getActivity();

                                    if (fragmentActivity != null) {
                                        Toast.makeText(fragmentActivity, R.string.success_remove_task, Toast.LENGTH_LONG).show();

                                        mListener.onTaskRemoved();
                                    }
                                } else {
                                    Snackbar.make(rootView, R.string.error_remove_task, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        loadTaskData(rootView);

        if (arguments.containsKey(ARG_NOTIFICTION) && arguments.getBoolean(ARG_NOTIFICTION)) {
            floatingActionButtonMenu.setVisibility(View.GONE);
        }

        return rootView;
    }

    private void loadTaskData(View rootView) {
        if (arguments.containsKey(ARG_ITEM_ID)) {
            // Load the content specified by the fragment arguments
            mItem = db.getTask(arguments.getLong(ARG_ITEM_ID));
        }

        if (mItem != null) {
            TaskType taskType = db.getTaskType(mItem.getType());

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mItem.getDateTime());

            ((TextView) rootView.findViewById(R.id.textViewTitle)).setText(mItem.getTitle());
            rootView.findViewById(R.id.viewTypeColor).setBackgroundColor(taskType.getColor());
            ((TextView) rootView.findViewById(R.id.textViewType)).setText(taskType.getType());
            ((TextView) rootView.findViewById(R.id.textViewDate)).setText(DateFormat.getDateFormat(getContext()).format(calendar.getTime()));
            ((TextView) rootView.findViewById(R.id.textViewTime)).setText(DateFormat.getTimeFormat(getContext()).format(calendar.getTime()));

            if (mItem.getDescription().length() == 0) {
                rootView.findViewById(R.id.textViewDescriptionHeader).setVisibility(View.GONE);
                rootView.findViewById(R.id.textViewDescription).setVisibility(View.GONE);
            } else {
                rootView.findViewById(R.id.textViewDescriptionHeader).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.textViewDescription).setVisibility(View.VISIBLE);
                ((TextView) rootView.findViewById(R.id.textViewDescription)).setText(mItem.getDescription());
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_TASK) {
            loadTaskData(getView());
        }
    }

    public interface OnFragmentInteractionListener {
        void onTaskRemoved();
    }
}
