package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class ViewJobsListsActivity extends AppCompatActivity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private ArrayList<Job> allPostDetailsList;
    private ArrayList<String> jobsToFetch;
    private JobListAdapter jobsAdapter;
    private DatabaseReference myListRef;

    private ListView allPostsList;
    private TextView savedJobsButton;
    private TextView allJobsButton;
    private TextView activeJobsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_job_lists);
        jobsToFetch = null;

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account

        currentUser = auth.getCurrentUser();
        allJobsButton = (TextView) findViewById(R.id.buttonViewAllJobs);
        savedJobsButton = (TextView) findViewById(R.id.buttonViewSavedJobs);
        activeJobsButton = (TextView) findViewById(R.id.buttonViewActiveJobsList);
        //Gonna hold all the jobs, must init for adaptor
        allPostDetailsList = new ArrayList<Job>();
        //Put the jobs into the adaptor
        //Find the listview widget and set up a connection to our ArrayList
        allPostsList = (ListView) findViewById(R.id.lvJobs);
        jobsAdapter = new JobListAdapter(ViewJobsListsActivity.this, allPostDetailsList);
        // The adaptor handles pushing each object in the ArrayList to the listview
        // but you MUST call jobsAdaptor.notifyDataSetChanged(); to update the listview
        allPostsList.setAdapter(jobsAdapter);
        String intentData = getIntent().getExtras().get("View").toString();
        switch (intentData){
            case "saved":
                viewSomeJobs(savedJobsButton, "savedjobs");
                break;
            case "active":
                viewSomeJobs(activeJobsButton, "activejobs");
                break;
            case "all":
            default:
                viewAllJobs(allJobsButton);
                break;
        }
        //This handles clicks on individual job items from the list
        // and bring you to a job specific page with details
        allPostsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Job selectedJob = (Job) jobsAdapter.getItem(position);
                Intent singleJobViewIntent = new Intent(ViewJobsListsActivity.this, ViewSingleJobActivity.class);
                singleJobViewIntent.putExtra("Job",selectedJob);
                startActivity(singleJobViewIntent);

            }
        });
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
            case R.id.viewPostsMenuPostJob:
                startActivity(new Intent(this, PostJobActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuMyProfile:
                startActivity( new Intent( ViewJobsListsActivity.this, ViewMyProfileActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuAllChats:
                startActivity(new Intent(ViewJobsListsActivity.this, ViewActiveChatsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuMyJobs:
                startActivity(new Intent(ViewJobsListsActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuAllJobs:
                if(getIntent().getExtras().get("View").toString().equals("saved")) {
                    getIntent().removeExtra("View");
                    getIntent().putExtra("View", "all");
                }
                recreate();
                return true;
            case R.id.viewPostsMenuSearch:
                Intent SearchIntent = new Intent(this, SearchActivity.class);
                startActivity(SearchIntent);
                return true;
            case R.id.viewPostsMenuJobsMap:
                Intent MapAllJobsViewIntent = new Intent(this, MapJobsActivity.class);
                startActivity(MapAllJobsViewIntent);
                return true;
            case R.id.viewPostsMenuSignOut:
                auth.signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // this will be for later maybe, feel free to remove
    public void gotoPostNewJob(View v){
        startActivity( new Intent( ViewJobsListsActivity.this, PostJobActivity.class));
        finish();
    }
    public void viewAllJobs(View v){
        myListRef = database.getReference("Jobs");
        jobsToFetch = null;
        myListRef.addValueEventListener(
                new JobListVEListener(ViewJobsListsActivity.this, allPostDetailsList, jobsAdapter));
    }
    public void viewSavedJobs(View v){
        viewSomeJobs(v, "savedjobs");
    }
    public void viewRequestedJobs(View v){
        viewSomeJobs(v, "requestedjobs");
    }
    public void viewActiveJobs(View v){ viewSomeJobs(v, "activejobs"); }
    public void viewSomeJobs(View v, String jobSet){
        jobsToFetch = new ArrayList<>();
        myListRef = database.getReference("Users").child(currentUser.getUid().toString()).child(jobSet).getRef();
        myListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Find each job made saved by the user
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    //make a list of the user's saved jobs
                    jobsToFetch.add(messageSnapshot.getValue().toString());
                }
                DatabaseReference myJobsRef = database.getReference("Jobs");
                myJobsRef.addListenerForSingleValueEvent(
                        new JobListVEListener(ViewJobsListsActivity.this, allPostDetailsList, jobsAdapter, jobsToFetch));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {/* idk what we would do*/ }
        });
    }
/********************* Switch to map view ********************/
    public void showMapOfJobs(View v){
        Intent MapJobsViewIntent = new Intent(this, MapJobsActivity.class);
        if(jobsToFetch != null) {
            MapJobsViewIntent.putStringArrayListExtra("JobsList", jobsToFetch);
        }
        startActivity(MapJobsViewIntent);
    }
}