package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class JobListViewActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    ArrayList<Job> allPostDetailsList;
    JobPostListAdapter jobsAdaptor;
    DatabaseReference myListRef;

    ListView allPostsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_list_view);

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
                    startActivity(new Intent(JobListViewActivity.this, LoginActivity.class));
                    finish();
                }else{
                    //user is logged in
                    currentUser = user;
                    final FirebaseDatabase database = FirebaseDatabase.getInstance();
                    // two ways to do this, only use one
                    myListRef = database.getReference("Jobs");
                    // DatabaseReference myUserRef = database.getReference("Users/"+user.getUid().toString()+"/jobs");

                    //Gonna hold all the jobs, must init for adaptor
                    allPostDetailsList = new ArrayList<Job>();
                    //Put the jobs into the adaptor
                    //Find the listview widget and set up a connection to our ArrayList
                    // The adaptor handles pushing each object in the ArrayList to the listview
                    allPostsList = (ListView) findViewById(R.id.lvJobs);
                    jobsAdaptor = new JobPostListAdapter(JobListViewActivity.this, allPostDetailsList);
                    allPostsList.setAdapter(jobsAdaptor);

                    myListRef.addListenerForSingleValueEvent(
                            new JobListVEListener(JobListViewActivity.this, allPostDetailsList, jobsAdaptor));

                    allPostsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                            Job selectedJob = (Job) jobsAdaptor.getItem(position);
                            Intent singleJobViewIntent = new Intent(JobListViewActivity.this, ViewSingleJobActivity.class);
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
                startActivity( new Intent( JobListViewActivity.this, ProfileActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenu2:
                startActivity(new Intent(JobListViewActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenu3:
                startActivity(new Intent(JobListViewActivity.this, JobListViewActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenu4:
                startActivity(new Intent(JobListViewActivity.this, ViewMySavedPostsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenu5:
                auth.signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // this will be for later maybe, feel free to remove
    public void gotoPostNewJob(View v){
        startActivity( new Intent( JobListViewActivity.this, PostJobActivity.class));
        finish();
    }
    public void viewAllJobs(View v){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myListRef = database.getReference("Jobs");
        allPostDetailsList.clear();
        jobsAdaptor.notifyDataSetChanged();
        myListRef.addListenerForSingleValueEvent(
                new JobListVEListener(JobListViewActivity.this, allPostDetailsList, jobsAdaptor));

    }
    public void viewSavedJobs(View v){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ArrayList<String> jobsToFetch = new ArrayList<String>();
        myListRef = database.getReference("Users").child(currentUser.getUid().toString()).child("savedjobs").getRef();

        allPostDetailsList.clear();
        jobsAdaptor.notifyDataSetChanged();
        myListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Find each job made by the user and add it to the listview
                // This is done with the ValueEventListener object above
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    //make a list of the user's saved jobs
                    jobsToFetch.add(messageSnapshot.getValue().toString());
                }
                //fetch all saved jobs
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // idk what we would do
            }
        });
        DatabaseReference myJobsRef = database.getReference("Jobs");
        myJobsRef.addListenerForSingleValueEvent(
                new JobListVEListener(JobListViewActivity.this, allPostDetailsList, jobsAdaptor, jobsToFetch));
    }
}