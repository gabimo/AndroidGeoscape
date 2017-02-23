package com.lawnscape;
/*
This just pulls the whole Jobs table, contained are many userids
Jobs are parsed in to JSON and parsed out to a custom local POJO Job
*/
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewAllJobsActivity extends Activity {

    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    ArrayList<Job> allPostDetailsList;
    JobPostListAdapter jobsAdaptor;

    ListView allPostsList;

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
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is not logged in
                    // launch login activity
                    startActivity(new Intent(ViewAllJobsActivity.this, LoginActivity.class));
                    finish();
                }else{
                    //user is logged in
                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    // two ways to do this, only use one
                    DatabaseReference myJobsRef = database.getReference("Jobs");
                    // DatabaseReference myUserRef = database.getReference("Users/"+user.getUid().toString()+"/jobs");

                    //Gonna hold all the jobs, must init for adaptor
                    allPostDetailsList = new ArrayList<Job>();
                    //Put the jobs into the adaptor
                    //Find the listview widget and set up a connection to our ArrayList
                    // The adaptor handles pushing each object in the ArrayList to the listview
                    allPostsList = (ListView) findViewById(R.id.lvMyPostsList);
                    jobsAdaptor = new JobPostListAdapter(ViewAllJobsActivity.this, allPostDetailsList);
                    allPostsList.setAdapter(jobsAdaptor);

                    // set this up to use after we find the personal job IDs
                    // The reason this is declared is so it does not appear visually as a nested listener
                    // Since this will actually be the place to handle output to the screen
                    // Until i figure out why I cant write to the higher scoped arraylist myPostDetailList and jobList
                    final ValueEventListener listenForJobPosts = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Add all the jobs to the array list
                            for(DataSnapshot jobNode: dataSnapshot.getChildren()) {
                                String title = (String) jobNode.child("title").getValue();
                                String location = (String) jobNode.child("location").getValue();
                                String description = (String) jobNode.child("description").getValue();
                                String userid = (String) jobNode.child("userid").getValue();
                                String postid = (String) jobNode.getKey().toString();
                                allPostDetailsList.add(new Job(title, location, description, userid, postid));
                                //Tell the listview adaptor to update the listview based on the ArrayList updates
                                jobsAdaptor.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError firebaseError) { }
                    };
                    myJobsRef.addListenerForSingleValueEvent(listenForJobPosts);

                    allPostsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                            Job selectedJob = (Job) jobsAdaptor.getItem(position);
                            Intent singleJobViewIntent = new Intent(ViewAllJobsActivity.this, ViewSingleJobActivity.class);
                            singleJobViewIntent.putExtra("Job",selectedJob);
                            startActivity(singleJobViewIntent);
                        }
                    });
                }
            }
        };
    }
    @Override
    public void onStart() {
        super.onStart();
        // Boiler plate Authentication
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Boiler plate Authentication
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
    /************** End LifeCycle ****************/
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
                startActivity( new Intent( ViewAllJobsActivity.this, ProfileActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenu2:
                startActivity(new Intent(ViewAllJobsActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenu3:
                startActivity(new Intent(ViewAllJobsActivity.this, ViewAllJobsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenu4:
                auth.signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // this will be for later maybe, feel free to remove
    public void gotoPostNewJob(View v){
        startActivity( new Intent( ViewAllJobsActivity.this, PostJobActivity.class));
        finish();
    }


}