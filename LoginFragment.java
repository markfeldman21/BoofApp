package com.markfeldman.boof;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.UserService;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private FragmentTransaction ft;
    private Button registerButton, resetButton, loginButton;
    EditText userName, password;
    private boolean isPopUpOpen;
    private static final String PREFS_LOGGED_IN = "AreYouLoggedInFile";

    public OnClickedListener listener;
    public LogInInterface loggedInListener;

    static interface OnClickedListener{
        public void buttonClicked(View v);
    }

    static interface LogInInterface{
        public void userLoggedIn(boolean loggedIn);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.listener = (OnClickedListener)activity;
        this.loggedInListener = (LogInInterface)activity;
    }

    public LoginFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        isPopUpOpen = false;
        if (savedInstanceState!=null){
            if (savedInstanceState.getBoolean("isDialogOpen")){
                resetPopUpWindow();
            }
        }
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        registerButton = (Button)view.findViewById(R.id.register_button);
        resetButton = (Button) view.findViewById(R.id.reset_button);
        password = (EditText)view.findViewById(R.id.fragment_login_password);
        userName = (EditText)view.findViewById(R.id.fragment_login_username);

        loginButton = (Button)view.findViewById(R.id.fragment_login_loginButton);
        registerButton.setOnClickListener(this);
        resetButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register_button:{
                listener.buttonClicked(v);
                break;
            }
            case R.id.reset_button:{
                isPopUpOpen = true;
                resetPopUpWindow();
                break;
            }
            case R.id.fragment_login_loginButton:{
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Logging In...");
                progressDialog.show();

                Backendless.UserService.login(userName.getText().toString(), password.getText().toString(), new AsyncCallback<BackendlessUser>() {
                    public void handleResponse(BackendlessUser user) {
                        SharedPreferences myPrefs = getActivity().getSharedPreferences(PREFS_LOGGED_IN, 0);
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.commit();

                        loggedInListener.userLoggedIn(true);
                        progressDialog.dismiss();
                    }
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getActivity(), "There was an Issue: " + fault, Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                },true);
            }

            break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isDialogOpen",isPopUpOpen);
    }

    public void resetPopUpWindow(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.reset_password, null);
        final EditText popUp = (EditText)view.findViewById(R.id.resetPassword);

        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(view);
        alert.setTitle("Reset Password");
        alert.setCancelable(true);

        alert.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Sending Reset To Email...");
                progressDialog.show();

                Backendless.UserService.restorePassword(popUp.getText().toString(), new AsyncCallback<Void>() {
                    public void handleResponse(Void response) {
                        Toast.makeText(getActivity(), "Check Your Email", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }

                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getActivity(), "Email Not Recognized", Toast.LENGTH_LONG).show();
                        isPopUpOpen = false;
                        progressDialog.dismiss();
                    }
                });
            }
        });

        alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isPopUpOpen = false;
            }
        });
        alert.create();
        alert.show();
    }
}
