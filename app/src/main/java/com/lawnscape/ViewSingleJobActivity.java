package com.lawnscape;
/*
This activity depends on the Intent class method putExtra(String, Job)
When calling this activity make sure that a job is passed to it appropriately
For an example please see ViewMyPostsActivity
 */

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ViewSingleJobActivity extends Activity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser currentUser;
    private Job jobPost;
    private TextView tvTitle;
    private TextView tvLoc;
    private TextView tvDesc;
    private TextView tvDate;
    private ImageView ivPhoto;
    private BootstrapButton chatWithPostersButton;
    private BootstrapButton saveButton;
    private BootstrapButton requestButton;
    private Button deleteButton;
    private Button editButton;
    // Create a storage reference from our app
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_job);
        //get firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ViewSingleJobActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //user is logged in
                    deleteButton = (Button) findViewById(R.id.buttonDeletePost);
                    requestButton = (BootstrapButton) findViewById(R.id.buttonRequestJob);
                    saveButton = (BootstrapButton) findViewById(R.id.buttonSaveJob);
                    editButton = (Button) findViewById(R.id.buttonEditPostDetails);
                    chatWithPostersButton = (BootstrapButton) findViewById(R.id.buttonChatWithPoster);

                    tvTitle = (TextView) findViewById(R.id.tvSingleJobTitle);
                    tvLoc = (TextView) findViewById(R.id.tvSingleJobLocation);
                    tvDesc = (TextView) findViewById(R.id.tvSingleJobDescription);
                    tvDate = (TextView) findViewById(R.id.tvSingleJobDate);
                    ivPhoto = (ImageView) findViewById(R.id.ivSingleJobPhoto);
                    Intent jobIntent = getIntent();
                    jobPost = jobIntent.getParcelableExtra("Job");
                    //This finds the photo data by the job id from firebase storage, nothing is passed around
                    StorageReference jobPhotoRef = storage.getReference().child("jobphotos").child(jobPost.getPostid());
                    jobPhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            // Pass it to Picasso to download, show in ImageView and caching
                            Picasso.with(ViewSingleJobActivity.this).load(uri.toString()).into(ivPhoto);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });

                    tvTitle.setText(jobPost.getTitle());
                    tvLoc.setText(jobPost.getLocation());
                    tvDesc.setText(jobPost.getDescription());
                    tvDate.setText(jobPost.getDate());
                    if(jobPost.getUserid().toString().equals(currentUser.getUid().toString())){
                        //hide
                        requestButton.setVisibility(View.INVISIBLE);
                        saveButton.setVisibility(View.INVISIBLE);
                        chatWithPostersButton.setVisibility(View.INVISIBLE);
                    }else{
                        //hide
                        deleteButton.setVisibility(View.INVISIBLE);
                        editButton.setVisibility(View.INVISIBLE);
                    }
                    DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admins");
                    adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(currentUser.getUid().toString())){
                                Button deleteButton = (Button) findViewById(R.id.buttonDeletePost);
                                Button editButton = (Button) findViewById(R.id.buttonEditPostDetails);
                                deleteButton.setText("Admin Delete");
                                editButton.setText("Admin Edit");
                                deleteButton.setVisibility(View.VISIBLE);
                                editButton.setVisibility(View.VISIBLE);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    });

                }
            }
        };
    }

    // Boiler Plate Authentication
    @Override
    public void onStart() {
        super.onStart();
        // Boiler Plate Authentication
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Boiler Plate Authentication
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    /************** End LifeCycle ****************/
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
            case R.id.viewSinglePostMenuSearch:
                startActivity(new Intent(ViewSingleJobActivity.this, SearchActivity.class));
                return true;
            case R.id.viewSinglePostMenuBackToJobsList:
                finish();
                return true;
            case R.id.viewSinglePostMenuSignOut:
                mAuth.signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void gotoPostNewJob(View v){
        startActivity( new Intent( ViewSingleJobActivity.this, PostJobActivity.class));
        finish();
    }

    public void deletePost(View v){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //remove the job from the list of all jobs with a listener
        DatabaseReference myJobRef = database.getReference("Jobs");
        myJobRef.addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this,jobPost.getPostid()));
        //remove the job from the users job list with a listener*
        DatabaseReference myUserJobsRef = database.getReference("Users").child(jobPost.getUserid()).child("jobs");
        myUserJobsRef.addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this, jobPost.getPostid()));
        finish();
    }

    public void saveJob(View v){
        //Dont save your own jobs
        if(!currentUser.getUid().toString().equals(jobPost.getUserid())) {
            DatabaseReference mySavedJobsRef = database.getReference("Users").child(currentUser.getUid().toString()).child("savedjobs");
            mySavedJobsRef.addListenerForSingleValueEvent(
                    new ToggleAddIDVEListener(ViewSingleJobActivity.this,jobPost.getPostid()));
        }
    }
    public void requestJob(View v){
        //Dont let a user request their own job
        if(!currentUser.getUid().toString().equals(jobPost.getUserid())) {
            DatabaseReference ref = database.getReference("Jobs").child(jobPost.getPostid()).child("requesters");
            ref.addListenerForSingleValueEvent(
                    new ToggleAddIDVEListener(ViewSingleJobActivity.this,currentUser.getUid().toString()));
            ref = database.getReference("Users").child(currentUser.getUid()).child("requestedjobs");
            ref.addListenerForSingleValueEvent(
                    new ToggleAddIDVEListener(ViewSingleJobActivity.this,jobPost.getPostid()));
        }
    }
    public void openChat(View v){
        Intent chatIntent = new Intent(ViewSingleJobActivity.this,ChatActivity.class);
        chatIntent.putExtra("otherid",jobPost.getUserid());
        startActivity(chatIntent);
        finish();
    }
    public void editJob(View v){
        setContentView(R.layout.activity_edit_job);
        EditText etTitle = (EditText) findViewById(R.id.etEditJobTitle);
        EditText etLocation = (EditText) findViewById(R.id.etEditJobLocation);
        EditText etDesc = (EditText) findViewById(R.id.etEditJobDescription);
        etTitle.setText(jobPost.getTitle());
        etLocation.setText(jobPost.getLocation());
        etDesc.setText(jobPost.getDescription());
    }
    public void postChanges(View v){
        DatabaseReference myUserJobRef = database.getReference("Jobs").child(jobPost.getPostid());
        myUserJobRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            TextView etTitle = (TextView) findViewById(R.id.etEditJobTitle);
            TextView etLocation = (TextView) findViewById(R.id.etEditJobLocation);
            TextView etDescription = (TextView) findViewById(R.id.etEditJobDescription);
            String newTitle = etTitle.getText().toString();
            String newLoc = etLocation.getText().toString();
            String newDesc = etDescription.getText().toString();
            // changes are made
            if(newDesc.equals("")){
                newDesc = "No description";
            }
            dataSnapshot.getRef().addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this,"title", newTitle));
            dataSnapshot.getRef().addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this,"location", newLoc));
            dataSnapshot.getRef().addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this,"description", newDesc));
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
