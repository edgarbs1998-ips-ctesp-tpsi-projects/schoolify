package com.pam.schoolify.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.github.clans.fab.FloatingActionButton;
import com.pam.schoolify.R;
import com.pam.schoolify.activity.AddTaskActivity;
import com.pam.schoolify.activity.TaskListActivity;
import com.pam.schoolify.util.SQLiteHelper;
import com.pam.schoolify.util.UserSession;
import com.pam.schoolify.util.database.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Main (calendar) fragment displayed on MainActivity
 */
public class CalendarFragment extends Fragment {

    private static final int REQUEST_ADD_TASK = 1;
    CalendarView calendarViewTasks;
    // Database helper
    private SQLiteHelper db;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CalendarFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        db = new SQLiteHelper(view.getContext());

        calendarViewTasks = view.findViewById(R.id.calendarViewTasks);
        calendarViewTasks.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();
                Intent intent = new Intent(getActivity(), TaskListActivity.class);
                intent.putExtra(TaskListFragment.EXTRA_SELECTED_DATE, clickedDayCalendar.getTimeInMillis());
                startActivity(intent);
            }
        });

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButtonAddTask);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddTaskActivity.class);
                startActivityForResult(intent, REQUEST_ADD_TASK);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CalendarView calendarView = view.findViewById(R.id.calendarViewTasks);
                try {
                    calendarView.setDate(Calendar.getInstance());
                } catch (OutOfDateRangeException e) {
                    e.printStackTrace();
                }
            }
        }, 500);

        loadCalendarData();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadCalendarData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_TASK) {
            loadCalendarData();
        }
    }

    /**
     * Function to load the calendar data from database
     */
    private void loadCalendarData() {
        List<EventDay> eventTask = new ArrayList<>();

        List<Task> taskList = db.getAllTasks(UserSession.getUser());

        Calendar calendar;
        for (Task task : taskList) {
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(task.getDateTime());

            eventTask.add(new EventDay(calendar, R.drawable.ic_calendar_task));
        }

        calendarViewTasks.setEvents(eventTask);
    }
}
