package com.example.vytuatus.streetlive;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vytuatus.streetlive.Utils.Utility;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * Created by vytuatus on 1/10/18.
 */

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private SignInButton mGoogleSignInButton;
    private GoogleApiClient mGoogleApiClient;

    private Button mEmailSignInButton;
    private Button mEmailCreateAccountButton;
    private EditText mEmailField;
    private EditText mPasswordField;
    private ProgressDialog mProgressDialog;
    private TextView mContinueWoSignTextView;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Assign fields
        mGoogleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailCreateAccountButton = (Button) findViewById(R.id.email_create_account_button);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        mContinueWoSignTextView = findViewById(R.id.continue_wo_sing_textView);

        // Set click listeners
        mGoogleSignInButton.setOnClickListener(this);
        mEmailSignInButton.setOnClickListener(this);
        mEmailCreateAccountButton.setOnClickListener(this);
        mContinueWoSignTextView.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                // present user with the google sign in UI
                googleSignIn();
                break;
            case R.id.email_sign_in_button:
                // sign in with Email and Password
                emailSignIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
            case R.id.email_create_account_button:
                createEmailAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
                break;
            case R.id.continue_wo_sing_textView:
                signInAsGuest();
                break;
        }
    }

    // Just log use app without sign in
    private void signInAsGuest() {

        // Update the shared prefs to show that user is just a guest
        Utility.saveUserSignInTypeInSharedPrefs(SignInActivity.this,
                getString(R.string.signInType_pref_guest_user));

        startActivity(new Intent(SignInActivity.this,
                MainActivity.class));

        finish();
    }

    /**
     * Create an account for the user for email and password login
     * @param email
     * @param password
     */
    private void createEmailAccount(String email, String password) {
        // If email or password is written incorrectly then just return
        if (!validateForm()){
            return;
        }

        showProgressDialog();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            // Update the shared prefs to show that user is an active user with user name and pass
                            Utility.saveUserSignInTypeInSharedPrefs(SignInActivity.this,
                                    getString(R.string.signInType_pref_active_user));

                            // Account Create successfully and also signed in
                            startActivity(new Intent(SignInActivity.this,
                                    MainActivity.class));

                            finish();
                        } else {
                            // Failed. Present user with info.
                            Log.w(TAG, "createuserWithEmail: failure", task.getException());
                            Toast.makeText(SignInActivity.this,
                                    task.getException().toString().split(":")[1],
                                    Toast.LENGTH_SHORT).show();
                        }

                        hideProgressDialog();
                    }
                });
    }

    /**
     * Present user with the google sign in UI
     */
    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Handle the google sign in action here
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...); in
        // singIn() method
        if (requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                // Google sign in successful. We can proceed to authenticate with Firbase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign in failed
                Log.e(TAG, "Google Sign in failed");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        showProgressDialog();
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to user. If succeeds, the authState
                        // listener will be notified and logic to handle sign in user can be
                        // handled in the listener
                        if (!task.isSuccessful()){
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            // Update the shared prefs to show that user is an active user with user name and pass
                            Utility.saveUserSignInTypeInSharedPrefs(SignInActivity.this,
                                    getString(R.string.signInType_pref_active_user));

                            startActivity(new Intent(SignInActivity.this,
                                    MainActivity.class));

                            finish();
                        }

                        hideProgressDialog();
                    }
                });
    }

    /**
     * Sign in user with email and password
     * @param email
     * @param password
     */
    private void emailSignIn(String email, String password) {

        // If email or password is written incorrectly then just return
        if (!validateForm()){
            return;
        }
        // Start sign in with email
        showProgressDialog();
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            // Update the shared prefs to show that user is an active user with user name and pass
                            Utility.saveUserSignInTypeInSharedPrefs(SignInActivity.this,
                                    getString(R.string.signInType_pref_active_user));
                            // Sign in worked! go to MainActivity
                            startActivity(new Intent(SignInActivity.this,
                                    MainActivity.class));

                            finish();
                        } else {
                            // Failed. Present user with info.
                            Log.w(TAG, "signInWithEmail", task.getException());
                            Toast.makeText(SignInActivity.this,
                                    task.getException().toString().split(":")[1],
                                    Toast.LENGTH_SHORT).show();
                        }

                        // User succeeded/failed to sign in. hide progress dialog
                        hideProgressDialog();
                    }
                });
    }

    // Helper method to validate user's imputed email and password
    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    // Show the progress dialog when user sign's in via email or google
    public void showProgressDialog(){
        if (mProgressDialog == null){
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    // Dismiss progress Dialog when user successfully/unsuccessfully logs in to the app
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
