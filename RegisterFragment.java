package com.markfeldman.boof;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.Backendless;

public class RegisterFragment extends Fragment {
    private Button createAccountButton;
    private EditText passwordOne,passwordTwo, username,email;
    private BackendlessUser userOne = new BackendlessUser();
    private RegisteredInterface registeredInterface;

    public RegisterFragment() {
        setRetainInstance(true);
    }

    static interface RegisteredInterface{
        public void isRegistered(boolean registered);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        createAccountButton = (Button)view.findViewById(R.id.createAccountButton);
        passwordOne = (EditText)view.findViewById(R.id.createUserPassword);
        passwordTwo = (EditText)view.findViewById(R.id.createUserPasswordTwo);
        username = (EditText)view.findViewById(R.id.createUserName);
        email = (EditText)view.findViewById(R.id.createUserEmail);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.getText().toString().isEmpty()||passwordOne.getText().toString().isEmpty()||
                        passwordTwo.getText().toString().isEmpty()||email.getText().toString().isEmpty()){
                        Toast.makeText(getActivity(),"Please Make Sure All Fields Are Filled Out",Toast.LENGTH_LONG).show();
                }else{
                    if(!passwordOne.getText().toString().equals(passwordTwo.getText().toString())){
                        Toast.makeText(getActivity(),"Passwords Do No Match",Toast.LENGTH_LONG).show();
                    }else{
                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage("Creating Account...");
                        progressDialog.show();

                        userOne.setEmail(email.getText().toString());
                        userOne.setPassword(passwordOne.getText().toString());
                        userOne.setProperty("userName",username.getText().toString());


                        Backendless.UserService.register(userOne, new AsyncCallback<BackendlessUser>() {
                            public void handleResponse(BackendlessUser registeredUser) {
                                Toast.makeText(getActivity(), "Registered!", Toast.LENGTH_LONG).show();
                                registeredInterface.isRegistered(true);
                                progressDialog.dismiss();
                            }

                            public void handleFault(BackendlessFault fault) {
                                Toast.makeText(getActivity(), "There was an Issue: " + fault, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        });

                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.registeredInterface = (RegisteredInterface)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        registeredInterface = null;
    }
}
