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

    ArrayList<String> myPostDetailsList;
    ArrayAdapter<String> jobsAdaptor;

    ListView myPostsList;


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
                    // user auth state is changed - user is not logged in
                    // launch login activity
                    startActivity(new Intent(ViewMyPostsActivity.this, LoginActivity.class));
                    finish();
                }else{
                    //user is logged in
                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    // two ways to do this, only use one
                    DatabaseReference myUserRef = database.getReference("Users").child(user.getUid().toString()).child("jobs");
                    // DatabaseReference myUserRef = database.getReference("Users/"+user.getUid().toString()+"/jobs");

                    //Gonna hold all the jobs, must init for adaptor
                    myPostDetailsList = new ArrayList<String>();
                    //Put the jobs into the adaptor
                    //Find the listview widget and set up a connection to our ArrayList
                    // The adaptor handles pushing each object in the ArrayList to the listview
                    myPostsList = (ListView) findViewById(R.id.lvMyPostsList);
                    jobsAdaptor = new ArrayAdapter<String>(ViewMyPostsActivity.this,
                            android.R.layout.simple_list_item_1, myPostDetailsList);
                    myPostsList.setAdapter(jobsAdaptor);

                    // set this up to use after we find the personal job IDs
                    // The reason this is declared is so it does not appear visually as a nested listener
                    // Since this will actually be the place to handle output to the screen
                    // Until i figure out why I cant write to the higher scoped arraylist myPostDetailList and jobList
                    final ValueEventListener listenForJobPosts = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Add all the jobs to the array list
                            String title = (String) dataSnapshot.child("title").getValue();
                            String location = (String) dataSnapshot.child("location").getValue();
                            String description = (String) dataSnapshot.child("description").getValue();
                            myPostDetailsList.add(title);
                            myPostDetailsList.add(location);
                            myPostDetailsList.add(description);
                            //Tell the listview adaptor to update the listview based on the ArrayList updates
                            jobsAdaptor.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(DatabaseError firebaseError) { }
                    };
                    myUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Find each job made by the user and add it to the listview
                            // This is done with the ValueEventListener object above
                            DatabaseReference myJobsRef = database.getReference("Jobs");
                            for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                                String job = messageSnapshot.getValue().toString();
                                //For each job posted by the user call the custom listener
                                myJobsRef.child(job).addListenerForSingleValueEvent(listenForJobPosts);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // idk what we would do
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