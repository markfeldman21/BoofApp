package com.markfeldman.boof;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import non_activity.DataManager;
import non_activity.GridCellAdapter;


public class CalendarFragment extends Fragment implements View.OnClickListener {
    private static final String tag = "MyCalendarActivity";
    private Button activityButton, contactsButton;
    private TextView currentMonth;
    private ImageView prevMonth,nextMonth;
    private GridView calendarView;
    private int month, year, currentMonthForCal;
    private boolean showPopUpAgain;
    private GridCellAdapter adapter;
    private Calendar calendar;
    private final android.text.format.DateFormat dateFormatter = new android.text.format.DateFormat();
    private static final String dateTemplate = "MMMM yyyy";
    public OnClickedCalListener listener;


    static interface OnClickedCalListener{
        public void buttonCalClicked(View v);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.listener = (OnClickedCalListener)activity;
    }


    public CalendarFragment() {
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        BackendlessUser user = Backendless.UserService.CurrentUser();
        String userEmail = user.getEmail();

        Toast.makeText(getActivity(), "IN CAL: " + userEmail, Toast.LENGTH_LONG).show();


        activityButton = (Button)view.findViewById(R.id.calActivityButton);
        contactsButton = (Button)view.findViewById(R.id.calContactsButton);


        calendar = Calendar.getInstance(Locale.getDefault());//instance of this timezone
        month = calendar.get(Calendar.MONTH)+1;
        year = calendar.get(Calendar.YEAR);
        currentMonthForCal = calendar.get(Calendar.MONTH);
        prevMonth = (ImageView)view.findViewById(R.id.prev);
        nextMonth = (ImageView)view.findViewById(R.id.next);
        calendarView = (GridView)view.findViewById(R.id.calendar);
        currentMonth = (TextView) view.findViewById(R.id.currentMonth);
        currentMonth.setText(dateFormatter.format(dateTemplate, calendar.getTime()));

        prevMonth.setOnClickListener(this);
        nextMonth.setOnClickListener(this);
        activityButton.setOnClickListener(this);
        contactsButton.setOnClickListener(this);

        adapter = new GridCellAdapter(getActivity(),month,year, currentMonthForCal);
        adapter.notifyDataSetChanged();
        calendarView.setAdapter(adapter);

        //clearSharedPreference(this);

        return view;
    }


    private void setGridCellAdapterToDate(int month, int year) {

        adapter = new GridCellAdapter(getActivity(), month, year,currentMonthForCal);

        calendar.set(year, month - 1, calendar.get(Calendar.DAY_OF_MONTH));
        currentMonth.setText(android.text.format.DateFormat.format(dateTemplate, calendar.getTime()));
        adapter.notifyDataSetChanged();
        calendarView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.prev:{
                if (month <= 1) {
                    month = 12;
                    year--;
                } else {
                    month--;
                }
                setGridCellAdapterToDate(month, year);
                break;
            }
            case R.id.next:{
                if (month > 11) {
                    month = 1;
                    year++;
                } else {
                    month++;
                }
                setGridCellAdapterToDate(month, year);
                break;
            }
            case R.id.calActivityButton:{
                listener.buttonCalClicked(v);
                break;
            }
            case R.id.calContactsButton:{
                listener.buttonCalClicked(v);
                break;
            }
        }


    /*public void popUpInfoMessage(){

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogStyle);
        alert.setTitle(R.string.popUpInfoTitle);
        alert.setMessage(R.string.popUpInfoMessage);
        alert.setCancelable(false);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("Got it! Don't Show Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences myPrefs = getSharedPreferences(PREFS_POP_UP,0);
                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putBoolean("showPopUp",false);
                editor.commit();
            }
        });
        alert.create();
        alert.show();
    }

    public void clearSharedPreference(Context context) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PREFS_POP_UP, Context.MODE_PRIVATE);
        editor = settings.edit();

        editor.clear();
        editor.commit();
    }
    */

    }
}
