package com.lawnscape;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
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

public class ViewMyPostsActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

    ArrayList<Job> myJobList;
    JobListAdapter jobsAdaptor;

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
                final FirebaseUser user = firebaseAuth.getCurrentUser();
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
                    myJobList = new ArrayList<Job>();
                    //Put the jobs into the adaptor
                    //Find the listview widget and set up a connection to our ArrayList
                    // The adaptor handles pushing each object in the ArrayList to the listview
                    myPostsList = (ListView) findViewById(R.id.lvMyPostsList);
                    jobsAdaptor = new JobListAdapter(ViewMyPostsActivity.this,myJobList);
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
                            String userid = (String) dataSnapshot.child("userid").getValue();
                            String lat = (String) dataSnapshot.child("latitude").getValue();
                            String lng = (String) dataSnapshot.child("longitude").getValue();
                            String postid = (String) dataSnapshot.getKey().toString();
                            myJobList.add(new Job(title, location,description,userid,postid, lat, lng));
                            //Tell the listview adaptor to update the listview based on the ArrayList updates
                            jobsAdaptor.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(DatabaseError firebaseError) { }
                    };
                    myUserRef.addListenerForSingleValueEvent(
                            new ValueEventListener() {
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
                    //click listener for each item on the list
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
                    // long click popup options menu for specific jobs
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
                                            DatabaseReference myUserJobsRef = database.getReference("Users").child(user.getUid()).child("jobs");
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
            case R.id.viewPostsMenuMyProfile:
                startActivity( new Intent( ViewMyPostsActivity.this, ViewMyProfileActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuAllChats:
                startActivity(new Intent(ViewMyPostsActivity.this, ViewActiveChatsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuMyJobs:
                startActivity(new Intent(ViewMyPostsActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuAllJobs:
                Intent allJobsViewIntent = new Intent(ViewMyPostsActivity.this, ViewJobsListsActivity.class);
                allJobsViewIntent.putExtra("View", "all");
                startActivity(allJobsViewIntent);
                finish();
                return true;
            case R.id.viewPostsMenuSavedPosts:
                Intent savedJobsViewIntent = new Intent(ViewMyPostsActivity.this, ViewJobsListsActivity.class);
                savedJobsViewIntent.putExtra("View", "saved");
                startActivity(savedJobsViewIntent);
                finish();
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
        startActivity( new Intent( ViewMyPostsActivity.this, PostJobActivity.class));
        finish();
    }
}