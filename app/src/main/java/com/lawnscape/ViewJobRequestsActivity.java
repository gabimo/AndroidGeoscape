package com.lawnscape;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewJobRequestsActivity extends Activity {
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Job selectedJob;

    ArrayList<User> userList;
    ArrayList<String> usersToDisplay;
    UserListAdapter reuesterAdapter;
    ListView allRequestersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_job_requests);
        selectedJob =(Job) getIntent().getExtras().get("Job");
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        currentUser = auth.getCurrentUser();
        userList = new ArrayList<User>();
        usersToDisplay = new ArrayList<String>();
        allRequestersList = (ListView) findViewById(R.id.lvJobRequesters);
        reuesterAdapter = new UserListAdapter(this, userList);
        allRequestersList.setAdapter(reuesterAdapter);

        DatabaseReference jobRequestersRef = database.getReference("Jobs").child(selectedJob.getPostid()).child("requesters");
        jobRequestersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot userid: dataSnapshot.getChildren()){
                    usersToDisplay.add(userid.getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {    }
        });
        jobRequestersRef = database.getReference("Users");
        jobRequestersRef.addListenerForSingleValueEvent(
                new UserListVEListener(ViewJobRequestsActivity.this, userList, usersToDisplay, reuesterAdapter));
    }
}
