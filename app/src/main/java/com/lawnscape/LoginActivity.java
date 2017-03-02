package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends Activity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Toast.makeText(LoginActivity.this, "Logged in",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, ViewMyProfileActivity.class));
                    finish();
                }
            }
        };
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
    /************** Switch to SIGN UP view ****************/
    public void signup(View v){
        //switch to sign up activity
        setContentView(R.layout.activity_sign_up);
    }
    /******************* Menu Handling *******************/
    //make the menu show up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.loginMenu1:
                setContentView(R.layout.activity_sign_up);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /*************** LOGIN ***************/
    public void login(final View v) {
        final EditText emailBox = (EditText) findViewById(R.id.etLoginEmail);
        final EditText passBox = (EditText) findViewById(R.id.etLoginPassword);
        String email = emailBox.getText().toString();
        String password = passBox.getText().toString();
        if (!email.equals("") && !password.equals("")) {
            v.setEnabled(false);
            emailBox.setEnabled(false);
            passBox.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                //  Log.w(TAG, "signInWithEmail", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                v.setEnabled(true);
                                emailBox.setEnabled(true);
                                passBox.setEnabled(true);
                            } else {
                                Intent i = new Intent(LoginActivity.this, ViewMyProfileActivity.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    });
        }
    }
    /*********** SIGN UP ***********/
    public void createAccount(final View v) {
        final EditText emailBox = (EditText) findViewById(R.id.etSignUpEmail);
        final EditText passBox = (EditText) findViewById(R.id.etSignUpEmail);
        String email = emailBox.getText().toString();
        String password = passBox.getText().toString();
        if (!email.equals("") && !password.equals("")) {
            v.setEnabled(false);
            emailBox.setEnabled(false);
            passBox.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            // Log.d("EVENT", "createUserWithEmail:onComplete:" + task.isSuccessful());
                            if (!task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Could Not Create Account",
                                        Toast.LENGTH_SHORT).show();
                                v.setEnabled(true);
                                emailBox.setEnabled(true);
                                passBox.setEnabled(true);

                            } else {
                                Toast.makeText(LoginActivity.this, "Account Created Successfully",
                                        Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, ViewMyProfileActivity.class));
                                finish();
                            }
                        }
                    });
        }
    }
    public void backToLogin(View v){
        //switch to login view
        setContentView(R.layout.activity_login);
    }

}
