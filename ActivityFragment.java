package com.markfeldman.boof;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import non_activity.DBActivityAdapter;
import non_activity.DBAdapter;
import non_activity.LocalActivity;
import non_activity.LocalPhoneNum;

public class ActivityFragment extends Fragment {
    public DBActivityAdapter myDB;
    EditText inputActivity;
    private View view;
    private Button saveButton, deleteAllButton;
    public ListView myList;
    public long rowClickedForEdit = -1;
    public boolean shouldWeEdit = false;
    private ArrayList<String> activitiesForWeb = new ArrayList<>();


    public ActivityFragment() {setRetainInstance(true);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activitiesForWeb.clear();
        final String userEmail = Backendless.UserService.CurrentUser().getEmail();
        view =  inflater.inflate(R.layout.fragment_activity, container, false);

        inputActivity = (EditText)view.findViewById(R.id.inputActivity);

        saveButton = (Button)view.findViewById(R.id.activitySaveButton);
        deleteAllButton = (Button)view.findViewById(R.id.activityDeleteAllButton);

        openDB();
        retrieveDataFromCloud(userEmail);
        populateListView();
        listViewItemCancel();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shouldWeEdit) {
                    myDB.updateRow(rowClickedForEdit, inputActivity.getText().toString());

                    populateListView();
                } else {
                    onClick_Add(v);
                }

                shouldWeEdit = false;
                inputActivity.setText("");
                inputActivity.setHint("ENTER ACTIVITY");
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        });

        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDeleteAll(v);
                shouldWeEdit = false;
                inputActivity.setText("");
                inputActivity.setHint("ENTER ACTIVITY");
            }
        });

        myList = (ListView)view.findViewById(R.id.activityListViewFragment);


        return view;
    }


    private void openDB(){
        myDB = new DBActivityAdapter(getActivity());
        myDB.open();
    }

    public void onClick_Add(View v){
        if (!TextUtils.isEmpty(inputActivity.getText().toString())){
            final String userEmail = Backendless.UserService.CurrentUser().getEmail();

            myDB.insertRow(inputActivity.getText().toString());


            //*** Trying to add new info added. This isn't right. Try pulling data in onCreateView
            //Then override and onPause method that will update the database in the cloud
        }else{
            Toast.makeText(getActivity(), "Enter a Name Please", Toast.LENGTH_LONG).show();
        }
        populateListView();
    }

    public void onClickDeleteAll(View v){
        myDB.deleteAll();
        populateListView();
    }

    public void deleteRow(View v){
        myList = (ListView)view.findViewById(R.id.activityListViewFragment);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                myDB.deleteRow(id);
                populateListView();
            }
        });
    }

    private void update(long id){
        Cursor cursor = myDB.getRow(id);
        if (cursor.moveToFirst()){
            String updateName = inputActivity.getText().toString();
            myDB.updateRow(id,updateName);
        }
        cursor.close();
    }

    private void listViewItemCancel(){
        myList = (ListView) view.findViewById(R.id.activityListViewFragment);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                inputActivity.setText("");
                inputActivity.setHint("ENTER ACTIVITY");
                shouldWeEdit = false;
                populateListView();
            }
        });
    }


    public void populateListView(){
        Cursor cursor = myDB.getAllRows();
        String[] fromFieldNames = new String[] {DBActivityAdapter.KEY_ACTIVITY};
        int[] toViewIDs = new int[] {R.id.customActivityRowActivity};

        CursorAdapter cursorAdapter;
        cursorAdapter = new CursorAdapter(getActivity(),R.layout.activity_custom_row,cursor,fromFieldNames,toViewIDs);

        myList = (ListView)view.findViewById(R.id.activityListViewFragment);
        myList.setAdapter(cursorAdapter);
    }

    class CursorAdapter extends SimpleCursorAdapter {
        private Cursor c;
        private Context context;

        public CursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            this.c = c;
            this.context = context;
        }

        @Override
        public View getView(int pos, View inView, ViewGroup parent) {
            View vix = inView;

            if (vix == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                vix = inflater.inflate(R.layout.activity_custom_row, parent,false);
            }
            this.c.moveToPosition(pos);

            String activity = this.c.getString(this.c.getColumnIndex(DBActivityAdapter.KEY_ACTIVITY));
            final String id = this.c.getString(this.c.getColumnIndex(DBActivityAdapter.KEY_ROWID));

            TextView textActivity = (TextView) vix.findViewById(R.id.customActivityRowActivity);
            textActivity.setText(activity);

            final TextView textCancel = (TextView)vix.findViewById(R.id.customActivityRowCancelText);
            textCancel.setVisibility(View.INVISIBLE);

            final TextView deleteText = (TextView)vix.findViewById(R.id.customActivityRowDeleteText);
            final TextView editInfo = (TextView)vix.findViewById(R.id.customActivityRowEditText);

            deleteText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    myDB.deleteRow(Integer.valueOf(id));
                    populateListView();
                }
            });

            editInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textCancel.setVisibility(View.VISIBLE);
                    inputActivity.setText("");
                    rowClickedForEdit = Long.valueOf(id);
                    shouldWeEdit = true;
                }
            });


            return vix;
        }


    }

    public void retrieveDataFromCloud(final String userEmail){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Retrieving Data...");
        progressDialog.show();

        String whereClause = "userEmailID = '"+ userEmail +"'";

        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause( whereClause );

        Backendless.Persistence.of(LocalActivity.class).find(dataQuery, new AsyncCallback<BackendlessCollection<LocalActivity>>(){
            @Override
            public void handleResponse( BackendlessCollection<LocalActivity> foundContacts )
            {
                for (LocalActivity temp : foundContacts.getData()){
                    Log.v("Test", "Crashhh in is Loop");
                    activitiesForWeb.add(temp.getActivity());
                }

                if (activitiesForWeb.isEmpty()){
                    Log.v("Test", "Crashhh in is Empty");
                    myDB.deleteAll();
                    populateListView();
                    Toast.makeText(getActivity(), "No Data Found", Toast.LENGTH_SHORT).show();
                }else{
                    Log.v("Test", "Crashhh in is ELSE");
                    populateDbFromWeb();
                    Toast.makeText(getActivity(), "LIST POPULATED", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void handleFault( BackendlessFault fault ) {
                Toast.makeText(getActivity(), "Error" + fault , Toast.LENGTH_SHORT).show();
            }
        });
        progressDialog.dismiss();
    }

    public void populateDbFromWeb(){
        myDB.deleteAll();
        for (int i = 0; i<activitiesForWeb.size();i++){
            myDB.insertRow(activitiesForWeb.get(i));
        }
        populateListView();
    }

    @Override
    public void onPause() {
        super.onPause();
        final String userEmail = Backendless.UserService.CurrentUser().getEmail();
        new DeleteBulkFromBackEnd().execute(userEmail);
        new saveDataToBackEnd().execute();
    }

    class DeleteBulkFromBackEnd extends AsyncTask<String,Void,Void> {
        @Override
        protected Void doInBackground(String... email) {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL("https://api.backendless.com/v1/data/bulk/LocalActivity?where=userEmailID%3D%27"+email[0]+"%27");
                urlConnection = (HttpURLConnection)url.openConnection();

                urlConnection.setRequestMethod("DELETE");
                urlConnection.setRequestProperty( "application-id","053736BF-B330-1393-FFF2-E06901B2B100" );
                urlConnection.setRequestProperty( "secret-key","BDBB52F2-F041-2385-FFDA-E95D3FC21E00" );
                urlConnection.setRequestProperty( "application-type", "REST" );

                urlConnection.connect();

                int response = urlConnection.getResponseCode();
                Log.v("h", "RESPONSE IS: " + response );

                return null;


            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
                Log.v("h","ERROR IN DELETE" + e.toString() );
            }finally {
                urlConnection.disconnect();
                Log.v("h","DISCONNECTED");
            }

            return null;
        }
    }

    class saveDataToBackEnd extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            activitiesForWeb.clear();
            final String userEmail = Backendless.UserService.CurrentUser().getEmail();
            Cursor cursor = myDB.getAllRows();

            if(cursor.moveToFirst()) {
                do {
                    String activity = cursor.getString(cursor.getColumnIndex(DBActivityAdapter.KEY_ACTIVITY));
                    Log.v("hey", "ACTIVITY IS " + activity);
                    activitiesForWeb.add(activity);
                } while (cursor.moveToNext());
            }

            for (int i=0; i<activitiesForWeb.size(); i++){
                LocalActivity localActivity = new LocalActivity(userEmail, activitiesForWeb.get(i));
                Log.v("hey", "ACTIVITY NOW IS " + activitiesForWeb.get(i));
                Backendless.Persistence.save(localActivity, new AsyncCallback<LocalActivity>() {
                    @Override
                    public void handleResponse(LocalActivity localActivity) {
                        Log.v("hey", "SAVED!");
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.v("hey", "NOT SAVED!" + backendlessFault);
                    }
                });
            }
            return null;
        }
    }

}
