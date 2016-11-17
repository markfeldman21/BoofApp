package com.markfeldman.boof;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;

import java.util.ArrayList;
import java.util.List;

import non_activity.DataManager;

public class HomePage extends AppCompatActivity implements HomeFragment.OnClickedListener, CalendarFragment.OnClickedCalListener {
    private FragmentTransaction ft;
    private Fragment fragment, calFrag,actFrag, contactsFrag, homeFrag;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    String[] drawerListItems;
    private static final String PREFS_LOGGED_IN = "AreYouLoggedInFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        String appVersion = "v1";
        Backendless.initApp(this, "053736BF-B330-1393-FFF2-E06901B2B100", "BDBB52F2-F041-2385-FFDA-E95D3FC21E00", appVersion);
        getCurrentLoggedInUser();
        //checkLogStatusOnSite();
        calFrag = new CalendarFragment();
        actFrag = new ActivityFragment();
        contactsFrag = new ContactsFragments();
        homeFrag = new HomeFragment();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {

            }else{
                fragment = homeFrag;
                // In case this activity was started with special instructions from an
                // Intent, pass the Intent's extras to the fragment as arguments
                fragment.setArguments(getIntent().getExtras());

                // Add the fragment to the 'fragment_container' FrameLayout
                getFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragment).commit();
            }
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolBarHome);
        drawerLayout = (DrawerLayout)findViewById(R.id.homePageDrawer);
        drawerList = (ListView)findViewById(R.id.homePageList);
        drawerListItems = getResources().getStringArray(R.array.activities);
        drawerList.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,drawerListItems));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0: {
                        fragment = calFrag;
                        break;
                    }
                    case 1: {
                        fragment = contactsFrag;
                        break;
                    }
                    case 2: {
                        fragment = homeFrag;
                        break;
                    }
                    case 3:{
                        fragment = actFrag;
                        break;
                    }
                    case 4:{

                        logOutOnSite();

                        Toast.makeText(getApplicationContext(), "Logged Out!", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(HomePage.this, MainLoginActivity.class);
                        SharedPreferences myPrefs = getSharedPreferences(PREFS_LOGGED_IN, 0);
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putBoolean("isLoggedIn", false);
                        editor.commit();
                        startActivity(i);
                        finish();
                        break;
                    }
                }
                ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container,fragment);
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
                drawerLayout.closeDrawer(drawerList);
            }
        });
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,myToolbar,R.string.drawer_open,R.string.drawer_close)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                syncState();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
        setSupportActionBar(myToolbar);
        myToolbar.setLogo(R.drawable.happy_dog_icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerToggle.syncState();
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                if (drawerLayout.isDrawerOpen(drawerList)){
                    drawerLayout.closeDrawer(drawerList);
                }else{
                    drawerLayout.openDrawer(drawerList);
                }
                return true;
            }case R.id.infoActionBar:{
                Toast.makeText(getApplicationContext(), "Hey", Toast.LENGTH_LONG).show();
            }
            default:return super.onOptionsItemSelected(item);
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
    public void buttonClicked(View v) {
        switch (v.getId()){
            case R.id.logoutButton:{
                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    public void handleResponse(Void response) {
                        Toast.makeText(getApplicationContext(), "Logged Out!", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(HomePage.this, MainLoginActivity.class);
                        SharedPreferences myPrefs = getSharedPreferences(PREFS_LOGGED_IN, 0);
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putBoolean("isLoggedIn", false);
                        editor.commit();
                        startActivity(i);
                        finish();
                    }

                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getApplicationContext(), "Did Not Log Out!", Toast.LENGTH_LONG).show();
                    }
                });
            }
            case R.id.calendarButton:{
                fragment = new CalendarFragment();
                break;
            }
            case R.id.activityButton:{
                fragment = new ActivityFragment();
                break;
            }
            case R.id.contactsButton:{
                fragment = new ContactsFragments();
                break;
            }
        }
        ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container,fragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    @Override
    public void buttonCalClicked(View v) {
        switch (v.getId()){
            case R.id.calActivityButton:{
                fragment = new ActivityFragment();
                break;
            }
            case R.id.calContactsButton:{
                fragment = new ContactsFragments();
                break;
            }
        }

        ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container,fragment);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    public void logOutOnSite(){
        final AsyncCallback<Void> logoutResponder = new AsyncCallback<Void>()
        {
            @Override
            public void handleResponse( Void aVoid ) {
            }

            @Override
            public void handleFault( BackendlessFault backendlessFault ) {
            }
        };
        Backendless.UserService.logout(logoutResponder);
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

    public void getCurrentLoggedInUser(){
        String currentUserObjectId = Backendless.UserService.loggedInUser();

        if (currentUserObjectId != null && currentUserObjectId.length() > 0) {
            BackendlessDataQuery dataQuery = new BackendlessDataQuery();
            dataQuery.setWhereClause("objectId = '" + currentUserObjectId + "'");

            Backendless.Persistence.of(BackendlessUser.class).find(dataQuery, new AsyncCallback<BackendlessCollection<BackendlessUser>>(){
                @Override
                public void handleResponse( BackendlessCollection<BackendlessUser> response )
                {
                    List<BackendlessUser> users = response.getCurrentPage();
                    if (users != null && users.size() > 0) {
                        BackendlessUser user = users.get(0);

                        try {
                            Backendless.UserService.setCurrentUser(user);
                            Toast.makeText(getApplicationContext(), "CURRENT USER: " + user.getEmail(),Toast.LENGTH_LONG).show();
                        } catch (BackendlessException e) {
                            Toast.makeText(getApplicationContext(), "ERROR: " + e,Toast.LENGTH_LONG).show();

                        }}

                }
                @Override
                public void handleFault( BackendlessFault fault )
                {

                }
            });

        }
    }
}
