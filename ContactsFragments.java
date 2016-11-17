package com.markfeldman.boof;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.SimpleCursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;

import org.apache.http.client.methods.HttpPost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import non_activity.DBAdapter;
import non_activity.DataManager;
import non_activity.LocalActivity;
import non_activity.LocalPhoneNum;

public class ContactsFragments extends Fragment {
    public DBAdapter myDB;
    EditText inputName,inputNumber;
    private View view;
    private Button saveButton, deleteAllButton;
    public ListView myList;
    public long rowClickedForEdit = -1;
    public boolean shouldWeEdit = false;
    private ArrayList<String> numbersForWeb = new ArrayList<>();
    private ArrayList<String> namesForWeb = new ArrayList<>();
    private final String API_URL = "https://api.backendless.com/v1/data/bulk/LocalPhoneNum?where=userEmailD%3D%27markfeldman21%40gmail.com%27";


    public ContactsFragments() {setRetainInstance(true);}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        namesForWeb.clear();
        numbersForWeb.clear();
        final String userEmail = Backendless.UserService.CurrentUser().getEmail();
        view =  inflater.inflate(R.layout.fragment_contacts_fragments, container, false);

        inputName = (EditText)view.findViewById(R.id.inputName);
        inputNumber = (EditText)view.findViewById(R.id.inputNumber);
        saveButton = (Button)view.findViewById(R.id.saveButton);
        deleteAllButton = (Button)view.findViewById(R.id.deleteAllButton);
        openDB();
        retrieveDataFromCloud(userEmail);
        populateListView();
        listViewItemCancel();



        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shouldWeEdit) {
                    myDB.updateRow(rowClickedForEdit,
                            inputName.getText().toString(),
                            inputNumber.getText().toString()
                    );
                    populateListView();
                } else {
                    onClick_Add(v);
                }

                shouldWeEdit = false;
                inputName.setText("");
                inputNumber.setText("");
                inputName.setHint("ENTER CONTACT NAME");
                inputNumber.setHint("ENTER CONTACT NUMBER");
                final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        });

        deleteAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDeleteAll(v);
                shouldWeEdit = false;
                inputName.setText("");
                inputNumber.setText("");
                inputName.setHint("ENTER CONTACT NAME");
                inputNumber.setHint("ENTER CONTACT NUMBER");
            }
        });

        myList = (ListView)view.findViewById(R.id.listViewFragment);

        return view;
    }

    private void openDB(){
        myDB = new DBAdapter(getActivity());
        myDB.open();
    }

    public void onClick_Add(View v){
        if (!TextUtils.isEmpty(inputName.getText().toString())){
            myDB.insertRow(inputName.getText().toString(), inputNumber.getText().toString());
        }else{
            Toast.makeText(getActivity(), "Please Enter A Name", Toast.LENGTH_LONG).show();
        }
        populateListView();
    }

    public void populateDbFromWeb(){
        myDB.deleteAll();
        for (int i = 0; i<namesForWeb.size();i++){
            myDB.insertRow(namesForWeb.get(i), numbersForWeb.get(i));
        }
        populateListView();
    }

    public void populateListView(){
        Cursor cursor = myDB.getAllRows();

        String[] fromFieldNames = new String[] {DBAdapter.KEY_NAME,DBAdapter.KEY_NUMBER};
        int[] toViewIDs = new int[] {R.id.customRowContactName,R.id.customRowContactNumber};

        CursorAdapter cursorAdapter;
        cursorAdapter = new CursorAdapter(getActivity(),R.layout.contacts_custom_row,cursor,fromFieldNames,toViewIDs);

        myList = (ListView)view.findViewById(R.id.listViewFragment);
        myList.setAdapter(cursorAdapter);
    }

    public void onClickDeleteAll(View v){
        myDB.deleteAll();
        populateListView();
    }


    private void listViewItemCancel(){
        myList = (ListView) view.findViewById(R.id.listViewFragment);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                inputName.setText("");
                inputNumber.setText("");
                inputName.setHint("ENTER CONTACT NAME");
                inputNumber.setHint("ENTER CONTACT NUMBER");
                shouldWeEdit = false;
                populateListView();
            }
        });
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
                vix = inflater.inflate(R.layout.contacts_custom_row, null);
            }
            this.c.moveToPosition(pos);

            String name = this.c.getString(this.c.getColumnIndex(DBAdapter.KEY_NAME));
            String num = this.c.getString(this.c.getColumnIndex(DBAdapter.KEY_NUMBER));
            final String id = this.c.getString(this.c.getColumnIndex(DBAdapter.KEY_ROWID));

            TextView textName = (TextView) vix.findViewById(R.id.customRowContactName);
            textName.setText(name);
            final TextView textNum = (TextView) vix.findViewById(R.id.customRowContactNumber);
            textNum.setText(num);
            final TextView textCancel = (TextView)vix.findViewById(R.id.customRowCancelText);
            textCancel.setVisibility(View.INVISIBLE);

            final TextView deleteText = (TextView)vix.findViewById(R.id.customRowDeleteText);
            final TextView editInfo = (TextView)vix.findViewById(R.id.customRowEditText);

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
                    inputName.setText("");
                    inputNumber.setText("");
                    inputName.setHint("ENTER NEW NAME");
                    inputNumber.setHint("ENTER NEW NUMBER");
                    rowClickedForEdit = Long.valueOf(id);
                    shouldWeEdit = true;
                }
            });

            textNum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    URI uri;
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + textNum.getText().toString()));
                    startActivity(intent);
                }
            });

            return vix;
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        final String userEmail = Backendless.UserService.CurrentUser().getEmail();
        new DeleteBulkFromBackEnd().execute(userEmail);
        new saveDataToBackEnd().execute();

    }



    class saveDataToBackEnd extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            Log.v("hey", "IN A SYNC");
            namesForWeb.clear();
            numbersForWeb.clear();
            final String userEmail = Backendless.UserService.CurrentUser().getEmail();
            Cursor cursor = myDB.getAllRows();

            if(cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_NAME));
                    String number = cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_NUMBER));
                    namesForWeb.add(name);
                    numbersForWeb.add(number);
                } while (cursor.moveToNext());
            }

            for (int i=0; i<namesForWeb.size(); i++){
                LocalPhoneNum localPhoneNum = new LocalPhoneNum(userEmail, numbersForWeb.get(i), namesForWeb.get(i));
                Backendless.Persistence.save(localPhoneNum, new AsyncCallback<LocalPhoneNum>() {

                    @Override
                    public void handleResponse(LocalPhoneNum localPhoneNum) {
                        Log.v("hey", "SAVED!");
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        Log.v("hey", "NOT SAVED!");
                    }
                });
            }
            return null;
        }
    }

    public void retrieveDataFromCloud(final String userEmail){
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Retrieving Data...");
        progressDialog.show();

        String whereClause = "userEmailID = '"+ userEmail +"'";

        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause( whereClause );

        Backendless.Persistence.of(LocalPhoneNum.class).find(dataQuery, new AsyncCallback<BackendlessCollection<LocalPhoneNum>>(){
            @Override
            public void handleResponse( BackendlessCollection<LocalPhoneNum> foundContacts )
            {
                for (LocalPhoneNum temp : foundContacts.getData()){
                    Log.v("Test", "Crashhh in is Loop");
                    namesForWeb.add(temp.getName());
                    numbersForWeb.add(temp.getPhoneNum());
                }

                if (namesForWeb.isEmpty()){
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

    class DeleteBulkFromBackEnd extends AsyncTask<String,Void,Void>{
        @Override
        protected Void doInBackground(String... email) {
            HttpURLConnection urlConnection = null;

            try {
                URL url = new URL("https://api.backendless.com/v1/data/bulk/LocalPhoneNum?where=userEmailID%3D%27"+email[0]+"%27");
                urlConnection = (HttpURLConnection)url.openConnection();

                urlConnection.setRequestMethod("DELETE");
                urlConnection.setRequestProperty( "application-id","053736BF-B330-1393-FFF2-E06901B2B100" );
                urlConnection.setRequestProperty( "secret-key","BDBB52F2-F041-2385-FFDA-E95D3FC21E00" );
                urlConnection.setRequestProperty( "application-type", "REST" );

                urlConnection.connect();

                int response = urlConnection.getResponseCode();
                Log.d("h", "RESPONSE IS: " + response );

                return null;


            } catch (MalformedURLException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
                Log.d("h","ERROR " + e.toString() );
            }finally {
                urlConnection.disconnect();
                Log.d("h","DISCONNECTED");
            }

            return null;
        }

    }
}
