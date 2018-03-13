package com.example.kerekesnora.tripmemory;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseReference;

    private Button mRegistrationButton;
    private AutoCompleteTextView mUserNameTextView, mEmailTextView, mPasswordTextView, mConfPasswordTextView;
    private Context mContext;
    private String mEmail, mName, mPassword;

    private String TAG = RegistrationActivity.class.getSimpleName();
    private static final String TAG_REG = "TAG_RGISTRATION";
    private boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        //initialize auth
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //start authstate listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                Log.d(TAG, "onAuthStateChanged() called with: firebaseAuth = [" + user + "]");

                if (user != null) {
                    // User is signed in
                    Log.d(TAG_REG, "onAuthStateChanged:signed_in:" + user.getUid() + "name: "+ user.getEmail());

                    saveUserInformation(mName, mEmail);

                } else {
                    // User is signed out
                    Log.d(TAG_REG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        mUserNameTextView = (AutoCompleteTextView) findViewById(R.id.user_name);
        mEmailTextView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordTextView = (AutoCompleteTextView) findViewById(R.id.password);
        mConfPasswordTextView = (AutoCompleteTextView) findViewById(R.id.confirm_password);

        mRegistrationButton = (Button) findViewById(R.id.registration_button);
        mRegistrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkConnected()) {
                    mContext = RegistrationActivity.this;
                    Toast.makeText(mContext,R.string.no_internet_connection,Toast.LENGTH_LONG).show();
                }else{
                    if (!verifyFields()) {
                        Context context = RegistrationActivity.this;
                        String message = "Check it!";
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    } else {
                        mName = mUserNameTextView.getText().toString().trim();
                        mEmail = mEmailTextView.getText().toString().trim();
                        mPassword = mPasswordTextView.getText().toString().trim();

                        createAccount(mEmail, mPassword);
                    }

                }
            }
        });
    }

    private void createAccount(final String email, final String password) {
        Log.d(TAG, "createAccount" + email);
        if (!isEmailValid(email)) {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG_REG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Toast.makeText(RegistrationActivity.this, R.string.auth_failed,
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegistrationActivity.this, R.string.auth_succ,
                            Toast.LENGTH_SHORT).show();
                    flag = true;
                    Log.d(TAG_REG, "reg completed");
                    //saveUserInformation(name,email);

                }
            }
        });
    }

    private void saveUserInformation(String name, String email) {

        final UserInformation userInformation = new UserInformation(name, email);
        FirebaseUser user = mAuth.getCurrentUser();
        Log.d(TAG_REG, "writing user info " + user.getUid());

        mDatabaseReference.child("user").child(user.getUid()).setValue(userInformation, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                Intent intent = new Intent(RegistrationActivity.this,MainActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //intent.putExtra("VISIBLE","user");
                startActivity(intent);
                finish();
            }
        });


    }

    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean verifyFields() {
        // Reset errors.
        mEmailTextView.setError(null);
        mUserNameTextView.setError(null);
        mPasswordTextView.setError(null);
        mConfPasswordTextView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailTextView.getText().toString();
        String name = mUserNameTextView.getText().toString();
        String password = mPasswordTextView.getText().toString();
        String comfPassword = mConfPasswordTextView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordTextView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordTextView;
            cancel = true;
        }

        // Check for a valid confirm password, if the user entered one.
        if (!TextUtils.isEmpty(comfPassword) && !isPasswordValid(comfPassword)) {
            mConfPasswordTextView.setError(getString(R.string.error_invalid_password));
            focusView = mConfPasswordTextView;
            cancel = true;
        }

        //Check for password field is equal with confirm password field
        if (!mPasswordTextView.getText().toString().equals(mConfPasswordTextView.getText().toString())) {
            mPasswordTextView.setError(getString(R.string.error_isnotequal_password));
            mConfPasswordTextView.setError(getString(R.string.error_isnotequal_password));
            focusView = mConfPasswordTextView;
            cancel = true;
        }

        //Check for name field is not empty
        if (TextUtils.isEmpty(name)) {
            mUserNameTextView.setError(getString(R.string.error_field_required));
            focusView = mUserNameTextView;
            cancel = true;
        }

        //Check for password field is not empty
        if (TextUtils.isEmpty(password)) {
            mPasswordTextView.setError(getString(R.string.error_field_required));
            focusView = mPasswordTextView;
            cancel = true;
        }

        //Check for comfirm password field is not empty
        if (TextUtils.isEmpty(comfPassword)) {
            mConfPasswordTextView.setError(getString(R.string.error_field_required));
            focusView = mConfPasswordTextView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailTextView.setError(getString(R.string.error_field_required));
            focusView = mEmailTextView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailTextView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailTextView;
            cancel = true;
        }

        if (cancel) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
