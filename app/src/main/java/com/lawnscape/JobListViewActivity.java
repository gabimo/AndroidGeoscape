package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    ArrayList<Job> allPostDetailsList;
    JobListAdapter jobsAdapter;
    DatabaseReference myListRef;

    ListView allPostsList;

    Button savedJobsButton;
    Button allJobsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_list_view);


        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account

        currentUser = auth.getCurrentUser();
        allJobsButton = (Button) findViewById(R.id.buttonViewAllJobs);
        savedJobsButton = (Button) findViewById(R.id.buttonViewSavedJobs);
        //Gonna hold all the jobs, must init for adaptor
        allPostDetailsList = new ArrayList<Job>();
        //Put the jobs into the adaptor
        //Find the listview widget and set up a connection to our ArrayList
        allPostsList = (ListView) findViewById(R.id.lvJobs);
        jobsAdapter = new JobListAdapter(JobListViewActivity.this, allPostDetailsList);
        // The adaptor handles pushing each object in the ArrayList to the listview
        // but you MUST call jobsAdaptor.notifyDataSetChanged(); to update the listview
        allPostsList.setAdapter(jobsAdapter);
        if(getIntent().getExtras().get("View").toString().equals("saved")) {
            viewSavedJobs(savedJobsButton);
        }else {
            viewAllJobs(allJobsButton);
        }
        //This handles clicks on individual job items from the list
        // and bring you to a job specific page with details
        allPostsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Job selectedJob = (Job) jobsAdapter.getItem(position);
                Intent singleJobViewIntent = new Intent(JobListViewActivity.this, ViewSingleJobActivity.class);
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
            case R.id.viewPostsMenuMyProfile:
                startActivity( new Intent( JobListViewActivity.this, ViewMyProfileActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuAllChats:
                startActivity(new Intent(JobListViewActivity.this, ViewAllChatsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuMyJobs:
                startActivity(new Intent(JobListViewActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuAllJobs:
                if(getIntent().getExtras().get("View").toString().equals("saved")) {
                    getIntent().removeExtra("View");
                    getIntent().putExtra("View", "all");
                }
                recreate();
                return true;
            case R.id.viewPostsMenuSavedPosts:
                if(getIntent().getExtras().get("View").toString().equals("all")) {
                    getIntent().removeExtra("View");
                    getIntent().putExtra("View", "saved");
                }
                recreate();
                return true;
            case R.id.viewPostsMenuSignOut:
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
        myListRef = database.getReference("Jobs");
        allPostDetailsList.clear();
        jobsAdapter.notifyDataSetChanged();
        myListRef.addListenerForSingleValueEvent(
                new JobListVEListener(JobListViewActivity.this, allPostDetailsList, jobsAdapter));
    }
    public void viewSavedJobs(View v){
        final ArrayList<String> jobsToFetch = new ArrayList<String>();
        myListRef = database.getReference("Users").child(currentUser.getUid().toString()).child("savedjobs").getRef();
        allPostDetailsList.clear();
        jobsAdapter.notifyDataSetChanged();
        myListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Find each job made saved by the user
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    //make a list of the user's saved jobs
                    jobsToFetch.add(messageSnapshot.getValue().toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // idk what we would do
            }
        });
        // now show the list of jobs that the user saved
        DatabaseReference myJobsRef = database.getReference("Jobs");
        myJobsRef.addListenerForSingleValueEvent(
                new JobListVEListener(JobListViewActivity.this, allPostDetailsList, jobsAdapter, jobsToFetch));
    }
}