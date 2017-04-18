package com.lawnscape;

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
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
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
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // launch login activity if user has no account
                    startActivity(new Intent(ViewJobsListsActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };


        allJobsButton = (TextView) findViewById(R.id.buttonViewAllJobs);
        savedJobsButton = (TextView) findViewById(R.id.buttonViewSavedJobs);
        activeJobsButton = (TextView) findViewById(R.id.buttonViewActiveJobsList);
        //These objects are respnsible for displaying posts
        allPostDetailsList = new ArrayList<>();
        allPostsList = (ListView) findViewById(R.id.lvJobs);
        jobsAdapter = new JobListAdapter(ViewJobsListsActivity.this, allPostDetailsList);
        // The adaptor handles pushing each object in the ArrayList to the listview
        // but you must call jobsAdaptor.notifyDataSetChanged(); to update the listview
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
        }
        //Clicking a list item will bring you to a page for that item
        allPostsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Job selectedJob = jobsAdapter.getItem(position);
                Intent singleJobViewIntent = new Intent(ViewJobsListsActivity.this, ViewSingleJobActivity.class);
                //The Job class implements 'Parcelable' in order to be passed as an intent extra
                singleJobViewIntent.putExtra("Job",selectedJob);
                startActivity(singleJobViewIntent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        //This invokes the Firebase.AuthStateListener Object mAuthListener and the code block inside it
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /************** End LifeCycle ****************/
    /******************* Menu Handling *******************/
    //make the menu show up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Creates the top action icons and the top menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_posts, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles item selection from the action bar and menu
        switch (item.getItemId()) {
            case R.id.viewPostsMenuPostJob:
                startActivity(new Intent(this, PostJobActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuMyProfile:
                startActivity( new Intent( ViewJobsListsActivity.this, ViewMyProfileActivity.class));
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
                    //getIntent().putExtra("View", "all");
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
                mAuth.signOut();
                startActivity(new Intent(ViewJobsListsActivity.this, LoginActivity.class));
                finish();
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