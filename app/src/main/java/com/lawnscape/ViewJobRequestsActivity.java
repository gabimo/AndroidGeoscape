package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewJobRequestsActivity extends Activity {
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Job selectedJob;

    private ArrayList<User> userList;
    private ArrayList<String> usersToDisplay;
    private UserListAdapter reuesterAdapter;
    private ListView allRequestersList;

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
        // need to add some onclick listeners
        //This handles clicks on individual user items from the list
        // and bring you to a job specific chat page with the user
        allRequestersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                User selectedUser = (User) reuesterAdapter.getItem(position);
                Intent chatIntent = new Intent(ViewJobRequestsActivity.this, ChatActivity.class);
                chatIntent.putExtra("otherid", selectedUser.getUserid());
                startActivity(chatIntent);
            }
        });
        // Hold down on a user in the chat list to get a popup
        allRequestersList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                                           long id) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(ViewJobRequestsActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.popup_assign_jobs, popup.getMenu());
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        final User selectedUser = (User) reuesterAdapter.getItem(position);
                        switch (item.getItemId()){
                            case R.id.longclickAssignJob:
                                //remove the job from the list of all jobs with a listener
                                DatabaseReference jobRef = database.getReference("Jobs").child(selectedJob.getPostid());
                                jobRef.child("activeworkers").addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewJobRequestsActivity.this,selectedUser.getUserid()));
                                DatabaseReference userRef = database.getReference("Users").child(selectedUser.getUserid());
                                userRef.child("activejobs").addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewJobRequestsActivity.this,selectedJob.getPostid()));
                                //Also delete the request
                                //return true;
                            case R.id.longclickDeleteChat:
                                //remove the job from the list of all jobs with a listener
                                jobRef = database.getReference("Jobs").child(selectedJob.getPostid()).child("requesters");
                                jobRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for(DataSnapshot reqUser: dataSnapshot.getChildren()) {
                                            System.out.println(reqUser.getKey());
                                            if (reqUser.getValue().toString().equals(selectedUser.getUserid())) {
                                                DatabaseReference userReqRef = database.getReference("Users").child(reqUser.getValue().toString()).child("requestedjobs");
                                                userReqRef.addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewJobRequestsActivity.this,selectedJob.getPostid()));
                                                reqUser.getRef().removeValue();
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {  }
                                });
                                userList.remove(selectedUser);
                                reuesterAdapter.notifyDataSetChanged();
                                return true;
                            case R.id.longclickViewProfile:
                                Intent viewProfileIntent = new Intent(ViewJobRequestsActivity.this, ViewUserProfileActivity.class);
                                viewProfileIntent.putExtra("UserID", selectedUser.getUserid());
                                startActivity(viewProfileIntent);
                                return true;
                        }
                        return true;
                    }
                });
                popup.show();//showing popup menu
                return true;
            }
        });
    }
}
