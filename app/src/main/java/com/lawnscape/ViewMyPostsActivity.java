package com.lawnscape;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewMyPostsActivity extends AppCompatActivity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private ArrayList<Job> myJobList;
    private JobListAdapter jobsAdaptor;
    private ListView myPostsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_posts);
        myJobList = new ArrayList<>();
        myPostsList = (ListView) findViewById(R.id.lvMyPostsList);
        jobsAdaptor = new JobListAdapter(ViewMyPostsActivity.this,myJobList);
        myPostsList.setAdapter(jobsAdaptor);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //make sure user is logged in and has an account
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // user auth state is changed - user is not logged in
                    // launch login activity
                    startActivity(new Intent(ViewMyPostsActivity.this, LoginActivity.class));
                    finish();
                }else{
                    DatabaseReference myUserRef = database.getReference("Users").child(currentUser.getUid().toString()).child("jobs");
                    //Async add user's own jobs to the list view
                    myUserRef.addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    //Find each job made by the user and add it to the listview
                                    // This is done with the ValueEventListener object above
                                    ArrayList<String> myJobIDs = new ArrayList<>();
                                    DatabaseReference myJobsRef = database.getReference("Jobs");
                                    for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                                        myJobIDs.add(messageSnapshot.getValue().toString());
                                        //For each job posted by the user call the custom listener
                                    }
                                    myJobsRef.addListenerForSingleValueEvent(new JobListVEListener(ViewMyPostsActivity.this,myJobList,jobsAdaptor,myJobIDs));
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {}
                            });
                }
            }
        };
        myPostsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
                Job selectedJob = (Job) jobsAdaptor.getItem(position);
                Intent singleJobViewIntent = new Intent(ViewMyPostsActivity.this, ViewSingleJobActivity.class);
                singleJobViewIntent.putExtra("Job",selectedJob);
                startActivity(singleJobViewIntent);
            }
        });
        myPostsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
                                           long id) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(ViewMyPostsActivity.this, view);
                popup.getMenuInflater().inflate(R.menu.popup_post_menu, popup.getMenu());
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        final Job selectedJob = (Job) jobsAdaptor.getItem(position);
                        switch (item.getItemId()){
                            case R.id.longclickDeletePost:
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                //remove the job from the list of all jobs with a listener
                                DatabaseReference myJobRef = database.getReference("Jobs");
                                myJobRef.addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewMyPostsActivity.this,selectedJob.getPostid()));
                                //remove the job from the users job list with a listener*
                                DatabaseReference myUserJobsRef = database.getReference("Users").child(currentUser.getUid()).child("jobs");
                                myUserJobsRef.addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewMyPostsActivity.this, selectedJob.getPostid()));
                                myJobList.remove(selectedJob);
                                jobsAdaptor.notifyDataSetChanged();
                                return true;
                            case R.id.longclickAssignJob:
                                //nothing yet
                                Intent assignJobIntent = new Intent(ViewMyPostsActivity.this, ViewJobRequestsActivity.class);
                                assignJobIntent.putExtra("Job", selectedJob);
                                startActivity(assignJobIntent);
                                return true;
                        }
                        return true;
                    }
                });
                popup.show();//showing popup menu
                return true;
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Check if user is logged  in
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
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra("View", "all");
                if (upIntent != null && NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder builder = TaskStackBuilder.create(this);
                    builder.addNextIntentWithParentStack(upIntent);
                    builder.startActivities();
                } else {
                    if (upIntent != null) {
                        upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        this.startActivity(upIntent);
                        this.finish();
                    } else {
                        upIntent = new Intent( this, ViewJobsListsActivity.class);
                        upIntent.putExtra("View", "all");
                        startActivity(upIntent);
                    }
                }
                return true;
        }
        return false;
    }
}