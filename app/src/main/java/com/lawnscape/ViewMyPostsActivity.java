package com.lawnscape;

import android.app.ListActivity;
import android.content.Intent;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.EventListener;

public class ViewMyPostsActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    ArrayList<String> jobsList;
    ArrayList<String> myPostDetailsList;
    ArrayAdapter<String> jobsAdaptor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_posts);

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
                    startActivity(new Intent(ViewMyPostsActivity.this, LoginActivity.class));
                    finish();
                }else{
                    //user is logged in
                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    // two ways to do this
                    DatabaseReference myUserRef = database.getReference("Users").child(user.getUid().toString()).child("jobs");
                    final DatabaseReference myJobsRef = database.getReference("Jobs");
                    //Gonna hold all the jobs
                    jobsList = new ArrayList<String>();
                    //Put the jobs into the adaptor
                    jobsList.add("My Jobs");
                    // set this up to use after we find the personal job IDs
                    final ValueEventListener listenForJobPosts = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Add all the jobs
                            myPostDetailsList = new ArrayList<String>();
                            System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                            for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                                String title = (String) messageSnapshot.child("title").getValue();
                                String location = (String) messageSnapshot.child("location").getValue();
                                String description = (String) messageSnapshot.child("description").getValue();

                                System.out.println("XX "+title+" :: "+location);
                                myPostDetailsList.add(title);
                                myPostDetailsList.add(location);
                                myPostDetailsList.add(description);

                            }
                            jobsAdaptor = new ArrayAdapter<String>(ViewMyPostsActivity.this,
                                    android.R.layout.simple_list_item_1, myPostDetailsList);
                            //find the list view to add posts to it
                            ListView myPostsList = (ListView) findViewById(R.id.lvMyPostsList);
                            myPostsList.setAdapter(jobsAdaptor);
                        }
                        @Override
                        public void onCancelled(DatabaseError firebaseError) { }
                    };
                    myUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                                jobsList.add(messageSnapshot.getValue().toString());
                            }
                            myJobsRef.addListenerForSingleValueEvent(listenForJobPosts);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    // List view needs adaptors for string arraylists

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
    /************** End LifeCycle ****************/
    public void signout(View v){
        auth.signOut();
    }
    /******************* Menu Handling *******************/
    //make the menu show up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_posts, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.viewPostsMenu1:
                startActivity( new Intent( ViewMyPostsActivity.this, ProfileActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenu2:
                auth.signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // this will be for later maybe, feel free to remove
    public void gotoPostNewJob(View v){
        startActivity( new Intent( ViewMyPostsActivity.this, PostJobActivity.class));
        finish();
    }
}