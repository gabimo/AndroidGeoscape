package com.lawnscape;
/*
This activity depends on the Intent class method putExtra(String, Job)
When calling this activity make sure that a job is passed to it appropriately
For an example please see ViewMyPostsActivity
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ViewSingleJobActivity extends Activity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseUser currentUser;
    private Job jobPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_job);
        Intent jobIntent = getIntent();
        jobPost = jobIntent.getParcelableExtra("Job");

        TextView tvTitle = (TextView) findViewById(R.id.tvSingleJobTitle);
        TextView tvLoc = (TextView) findViewById(R.id.tvSingleJobLocation);
        TextView tvDesc = (TextView) findViewById(R.id.tvSingleJobDescription);

        tvTitle.setText(jobPost.getTitle());
        tvLoc.setText(jobPost.getLocation());
        tvDesc.setText(jobPost.getDescription());

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
                    startActivity(new Intent(ViewSingleJobActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //user is logged in
                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    Button deleteButton = (Button) findViewById(R.id.buttonDeletePost);
                    Button requestButton = (Button) findViewById(R.id.buttonRequestJob);
                    Button saveButton = (Button) findViewById(R.id.buttonSaveJob);
                    Button chatWithPostersButton = (Button) findViewById(R.id.buttonChatWithPoster);
                    if(jobPost.getUserid().toString().equals(currentUser.getUid().toString())) {
                        deleteButton.setVisibility(View.VISIBLE);
                        requestButton.setVisibility(View.INVISIBLE);
                        saveButton.setVisibility(View.INVISIBLE);
                        chatWithPostersButton.setVisibility(View.INVISIBLE);
                    }

                }
            }
        };
    }

    // Boiler Plate Authentication
    @Override
    public void onStart() {
        super.onStart();
        // Boiler Plate Authentication
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Boiler Plate Authentication
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
        inflater.inflate(R.menu.menu_single_post_view, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.viewSinglePostMenuMyProfile:
                startActivity( new Intent( ViewSingleJobActivity.this, ViewMyProfileActivity.class));
                finish();
                return true;
            case R.id.viewSinglePostMenuChat:
                if(!currentUser.getUid().toString().equals(jobPost.getUserid())) {
                    Intent chatIntent = new Intent(ViewSingleJobActivity.this, ChatActivity.class);
                    chatIntent.putExtra("otherid", jobPost.getUserid());
                    startActivity(chatIntent);
                    finish();
                }
                return true;
            case R.id.viewSinglePostMenuMyJobs:
                startActivity(new Intent(ViewSingleJobActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.viewSinglePostMenuBackToJobsList:
                finish();
                return true;
            case R.id.viewSinglePostMenuSignOut:
                auth.signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    // this will be for later maybe, feel free to remove
    public void gotoPostNewJob(View v){
        startActivity( new Intent( ViewSingleJobActivity.this, PostJobActivity.class));
        finish();
    }

    public void deletePost(View v){
        /*
        Firebase does this thing where it wants you to push data to the DB
        and you get a unique post ID like Kdksk12sskw-2k_sk3mwk__jdk3k
        but you cant just get the post and delete it like this next line of this comment
       -- FirebaseDatabase.getInstance().getReference("Kdksk12sskw-2k_sk3mwk__jdk3k").removeValue();
        you have to instead delete it like I have implemented below with listeners
        */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //remove the job from the list of all jobs with a listener
        DatabaseReference myJobRef = database.getReference("Jobs");
        myJobRef.addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this,jobPost.getPostid()));
        //remove the job from the users job list with a listener*
        DatabaseReference myUserJobsRef = database.getReference("Users").child(currentUser.getUid()).child("jobs");
        myUserJobsRef.addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this, jobPost.getPostid()));
        finish();
    }

    public void saveJob(View v){
        /* before saving a job, check to make sure it isnt already saved to aoid duplication */
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference mySavedJobsRef = database.getReference("Users").child(currentUser.getUid().toString()).child("savedjobs");
        //Dont save your own jobs
        if(!currentUser.getUid().toString().equals(jobPost.getUserid())) {
            mySavedJobsRef.addListenerForSingleValueEvent(
                    new ToggleAddIDVEListener(ViewSingleJobActivity.this,jobPost.getPostid()));
        }
    }
    public void requestJob(View v){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference jobRequesterListRef = database.getReference("Jobs").child(jobPost.getPostid()).child("requesters");
        //Dont let a user request their own job
        if(!currentUser.getUid().toString().equals(jobPost.getUserid())) {
            jobRequesterListRef.addListenerForSingleValueEvent(
                    new ToggleAddIDVEListener(ViewSingleJobActivity.this,currentUser.getUid().toString())
            );
        }
    }
    public void openChat(View v){
        Intent chatIntent = new Intent(ViewSingleJobActivity.this,ChatActivity.class);
        chatIntent.putExtra("otherid",jobPost.getUserid());
        startActivity(chatIntent);
        finish();
    }
}
