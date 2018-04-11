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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    //Firebase:
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseReference;

    //Member:
    private Button mLoginButton, mRegisterButton, mForgotPasswordButton;
    private AutoCompleteTextView mEmailTextView, mPasswordTextView;
    private Context mContext;
    private String mEmail, mPassword;

    //Debug:
    private String TAG = RegistrationActivity.class.getSimpleName();
    private static final String TAG_REG = "TAG_RGISTRATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                    Log.d(TAG_REG, "onAuthStateChanged:signed_in:" + user.getUid() + "name: " + user.getEmail());
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    //intent2.putExtra("VISIBLE","user");
                    startActivity(intent);
                } else {
                    // User is signed out
                    Log.d(TAG_REG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        mEmailTextView = (AutoCompleteTextView) findViewById(R.id.email_login);
        mPasswordTextView = (AutoCompleteTextView) findViewById(R.id.password_login);

        mLoginButton = (Button) findViewById(R.id.logIn_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkConnected()) {
                    mContext = LoginActivity.this;
                    Toast.makeText(mContext,R.string.no_internet_connection,Toast.LENGTH_LONG).show();
                }else{
                    if (!verifyFields()) {
                        Context context = LoginActivity.this;
                        String message = "Check it!";
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    } else {
                        mEmail = mEmailTextView.getText().toString().trim();
                        mPassword = mPasswordTextView.getText().toString().trim();

                        signIn(mEmail, mPassword);
                    }

                }
            }
        });

        mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
                startActivity(intent);
            }
        });

        mForgotPasswordButton = (Button) findViewById(R.id.forgot_button);
        mForgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void signIn(String email, String password){
        Log.d(TAG, "singIn" + email);
        if(!isEmailValid(email)){
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, R.string.sign_in_failed, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    //finish();
                }
                else{
                    Toast.makeText(LoginActivity.this, R.string.sign_in_succ, Toast.LENGTH_SHORT).show();
                    Intent intent2 = new Intent(LoginActivity.this, MainActivity.class);
                    intent2.putExtra("VISIBLE","user");
                    startActivity(intent2);
                    //finish();
                }

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
        mPasswordTextView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailTextView.getText().toString();
        String password = mPasswordTextView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordTextView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordTextView;
            cancel = true;
        }

        //Check for password field is not empty
        if (TextUtils.isEmpty(password)) {
            mPasswordTextView.setError(getString(R.string.error_field_required));
            focusView = mPasswordTextView;
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


}
