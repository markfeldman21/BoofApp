package com.markfeldman.boof;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.backendless.BackendlessUser;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class MainLoginActivity extends AppCompatActivity implements LoginFragment.LogInInterface,LoginFragment.OnClickedListener,
        RegisterFragment.RegisteredInterface {
    private FragmentTransaction ft;

    private BackendlessUser userOne = new BackendlessUser();
    private boolean loggedIn;
    private static final String PREFS_LOGGED_IN = "AreYouLoggedInFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);
        String appVersion = "v1";
        Backendless.initApp(this, "053736BF-B330-1393-FFF2-E06901B2B100", "BDBB52F2-F041-2385-FFDA-E95D3FC21E00", appVersion);


        if (findViewById(R.id.content_frame) != null) {
            if (savedInstanceState != null) {
                return;
            }

            checkLogStatusOnSite();

            SharedPreferences prefs = getSharedPreferences(PREFS_LOGGED_IN,0);
            loggedIn = prefs.getBoolean("isLoggedIn",false);
            if (loggedIn){
                Toast.makeText(getApplicationContext(), "Logged In", Toast.LENGTH_LONG).show();
                Intent i = new Intent(MainLoginActivity.this,HomePage.class);
                startActivity(i);
                finish();
            }else {
                LoginFragment firstFragment = new LoginFragment();
                firstFragment.setArguments(getIntent().getExtras());
                ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, firstFragment);
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }
        }
    }

    @Override
    public void buttonClicked(View v) {
        switch (v.getId()){
            case R.id.register_button:{
                RegisterFragment registerFragment = new RegisterFragment();
                ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, registerFragment);
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void isRegistered(boolean registered) {
        if (registered){
            getFragmentManager().popBackStack();
            Toast.makeText(getApplicationContext(),"Registered!",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void userLoggedIn(boolean loggedIn) {
        if (loggedIn){
            Toast.makeText(getApplicationContext(),"Logged In Succesful!",Toast.LENGTH_LONG).show();
            Intent i = new Intent(MainLoginActivity.this,HomePage.class);
            startActivity(i);
            finish();
        }
    }

    public void checkLogStatusOnSite(){
        AsyncCallback<Boolean> isValidLoginCallBack = new AsyncCallback<Boolean>() {
            @Override
            public void handleResponse(Boolean aBoolean) {
                Toast.makeText(getApplicationContext(), "Boolean is: " + aBoolean, Toast.LENGTH_LONG).show();
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                Toast.makeText(getApplicationContext(), "Boolean Error: " + backendlessFault, Toast.LENGTH_LONG).show();

            }
        };

        Backendless.UserService.isValidLogin(isValidLoginCallBack);
    }
}