package com.zaske.about_steve.aboutsteve.ui.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.zaske.about_steve.aboutsteve.Constants;
import com.zaske.about_steve.aboutsteve.R;
import com.zaske.about_steve.aboutsteve.ui.MainActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = LoginActivity.class.getSimpleName();
    @BindView(R.id.passwordLoginButton) Button mPasswordLoginButton;
    @BindView(R.id.emailEditText) EditText mEmailEditText;
    @BindView(R.id.passwordEditText) EditText mPasswordEditText;
    @BindView(R.id.registerTextView) TextView mRegisterTextView;
    private ProgressDialog mAuthProgressDialog;

    private FirebaseAuth mAuth;

    //This is the Auth State listener object that will watch for when a user auth state changes
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        mRegisterTextView.setOnClickListener(this);
        mPasswordLoginButton.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();

        //check for previous user logins.  Auto-fill in
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEmailEditText.setText(sharedPreferences.getString(Constants.PREFERENCES_PREVIOUS_USER_KEY, ""));

        //Set the location of the cursor
        if (android.text.TextUtils.isEmpty(mEmailEditText.getText())){
            mEmailEditText.requestFocus();
        } else {
            mPasswordEditText.requestFocus();
        }

        createAuthProgressDialog();

        //Now we create a Auth State Listener. This will handle moving a user to other activities
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);

                    // NEW TASK FLAG - makes the activity we're going to be on the stack history
                    //CLEAR TASK - makes the activity we're coming from NOT be on the stack history.
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); //destroys the Login Activity so we cannot go back
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        //This line is sort of important.  Without it, the Auth State Listener will NOT be turned on.
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mRegisterTextView) {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
            finish();
        }
        if (view == mPasswordLoginButton) {
            loginWithPassword();
        }
    }

    private void loginWithPassword() {
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();
        if (email.equals("")) {
            mEmailEditText.setError("Please enter your email");
            return;
        }
        if (password.equals("")) {
            mPasswordEditText.setError("Password cannot be blank");
            return;
        }

        //Show the dialog
        mAuthProgressDialog.show();

        //This calls the method to login to Firebase User Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    //Event listener fires when completed
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mAuthProgressDialog.dismiss();
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        //Notice that we do not do ANYTHING if the user is authenticated correctly.
                        //Instead we let the AuthStateListener handle the next step

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });  //end of event listener
    } //end of loginWithPassword

    private void createAuthProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading...");
        mAuthProgressDialog.setMessage("Authenticating with Firebase...");
        mAuthProgressDialog.setCancelable(false);
    }
}
