package com.markfeldman.boof;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;



public class HomeFragment extends Fragment implements View.OnClickListener {
    private Button logoutButton, calendarButton,contactsButton, activityButton;
    public OnClickedListener listener;


    static interface OnClickedListener{
        public void buttonClicked(View v);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.listener = (OnClickedListener)activity;
    }

    public HomeFragment() {
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        logoutButton = (Button)view.findViewById(R.id.logoutButton);
        calendarButton = (Button) view.findViewById(R.id.calendarButton);
        contactsButton = (Button)view.findViewById(R.id.contactsButton);
        activityButton = (Button)view.findViewById(R.id.activityButton);

        activityButton.setOnClickListener(this);
        contactsButton.setOnClickListener(this);
        calendarButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.logoutButton:{
                listener.buttonClicked(v);
                break;
            }
            case R.id.calendarButton:{
                listener.buttonClicked(v);
                break;
            }
            case R.id.activityButton:{
                listener.buttonClicked(v);
                break;
            }
            case R.id.contactsButton:{
                listener.buttonClicked(v);
                break;
            }

        }
    }
}
