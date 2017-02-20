package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends Activity {
    //FireBase init
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //view things init
    EditText username;
    EditText passfield;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //init the login edit fields
        username = (EditText) findViewById(R.id.etSignUpEmail);
        passfield = (EditText) findViewById(R.id.etSignUpPassword);

        //Track when users sign in and out
        mAuth = FirebaseAuth.getInstance();
        //AuthStateListener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    System.out.println("Somehow logged in as " + user.getEmail().toString());
                } else {
                    // User is signed out
                    //Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    public void createAccount(View v) {
        String email = username.getText().toString().toLowerCase();
        String password = passfield.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        // Log.d("EVENT", "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Could Not Create Account",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Account Created Successfully",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignUpActivity.this, ProfileActivity.class));
                            finish();
                        }
                    }
                });
    }

    public void login(View v){
        //switch to login activity
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}