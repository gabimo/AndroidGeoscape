package com.lawnscape;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PostJobActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(PostJobActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //user is logged in
                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                }
            }
        };
    }
    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    public void postJob(View v){
        // grab the widgets as objects
        int success = 0;
        TextView etTitle = (TextView) findViewById(R.id.etNameProfileSettings);
        TextView etLocation = (TextView) findViewById(R.id.etLocation);
        TextView etDescription = (TextView) findViewById(R.id.etJobDescription);

        String newTitle = etTitle.getText().toString();
        String newLoc = etLocation.getText().toString();
        String newDesc = etDescription.getText().toString();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myJobsRef = database.getReference("Jobs/");

        // Add a job
        if(!newTitle.isEmpty()&&(!newLoc.isEmpty())){
            DatabaseReference newJobRef = myJobsRef.push();
            if(!newDesc.isEmpty()){
                newDesc = "No description";
            }
            newJobRef.setValue(new Job(newTitle, newLoc, newDesc));
        }
        finish();
    }
}
