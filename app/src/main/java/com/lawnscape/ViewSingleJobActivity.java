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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
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

import java.util.ArrayList;

public class ViewSingleJobActivity extends AppCompatActivity {

    // Create a storage reference from our app
    private FirebaseStorage storage;
    private FirebaseDatabase database;
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
    private Boolean isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_single_job);
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
                }else{
                    storage = FirebaseStorage.getInstance();
                    database = FirebaseDatabase.getInstance();
                    Intent jobIntent = getIntent();
                    jobPost = jobIntent.getParcelableExtra("Job");

                    tvTitle.setText(jobPost.getTitle());
                    tvLoc.setText(jobPost.getLocation());
                    tvDesc.setText(jobPost.getDescription());
                    tvDate.setText(jobPost.getDate());

                    //This finds the photo data by the job id from firebase storage, nothing is passed around
                    StorageReference jobPhotoRef = storage.getReference().child("jobphotos").child(jobPost.getPostid());
                    jobPhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            // Pass it to Picasso to download, show in ImageView and caching
                            Picasso.with(ViewSingleJobActivity.this).load(uri.toString()).into(ivPhoto);
                        }
                    });
                    if(jobPost.getUserid().toString().equals(currentUser.getUid().toString())){
                        //hide buttons used by workers
                        requestButton.setVisibility(View.INVISIBLE);
                        saveButton.setVisibility(View.INVISIBLE);
                        chatWithPostersButton.setVisibility(View.INVISIBLE);
                    }else{
                        //hide buttons used by job poster
                        deleteButton.setVisibility(View.INVISIBLE);
                        editButton.setVisibility(View.INVISIBLE);

                    }
                    DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admins");
                    adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(currentUser.getUid().toString())){
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
    public boolean onCreateOptionsMenu( Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_single_post_view, menu);

        final MenuItem favoritePostMenuItem = menu.findItem(R.id.viewSinglePostMenuFavorite);
        //Depends on lifecycle activities so currentUser objct isnt null
        if(!currentUser.getUid().equals(jobPost.getUserid())) {
            MenuItem editPostMenuItem = menu.findItem(R.id.viewSinglePostMenuEditPost);
            MenuItem deletePostMenuItem = menu.findItem(R.id.viewSinglePostMenuDelete);
            editPostMenuItem.setVisible(false);
            deletePostMenuItem.setVisible(false);

            DatabaseReference postFavoritedRef;
            postFavoritedRef = database.getReference("Users").child(currentUser.getUid().toString()).child("savedjobs").getRef();
            postFavoritedRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Find each job made saved by the user
                    if(dataSnapshot.hasChild(jobPost.getPostid())){
                        favoritePostMenuItem.setIcon(R.drawable.unfavorite_icon);
                        isFavorite = true;
                    }else{
                        isFavorite = false;
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {/* idk what we would do*/ }
            });
        }else{
            //Current user's own post
            favoritePostMenuItem.setVisible(false);
        }
        //Creates a back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.viewSinglePostMenuFavorite:
                if(isFavorite) {
                    item.setIcon(R.drawable.favorite_icon);
                    saveJob(null);
                }else{
                    item.setIcon(R.drawable.unfavorite_icon);
                    saveJob(null);
                }
                isFavorite = !isFavorite;
                return true;
            case R.id.viewSinglePostMenuEditPost:
                editJob(null);
                return true;
            case R.id.viewSinglePostMenuDelete:
                deletePost(null);
                return true;
            case R.id.viewSinglePostMenuMyProfile:
                startActivity( new Intent( ViewSingleJobActivity.this, ViewMyProfileActivity.class));
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
                return true;
            case R.id.viewSinglePostMenuSearch:
                startActivity(new Intent(ViewSingleJobActivity.this, SearchActivity.class));
                return true;
            case R.id.viewSinglePostMenuSignOut:
                mAuth.signOut();
                return true;
            default:
                finish();
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
        DatabaseReference mySavedJobsRef = database.getReference("Users").child(currentUser.getUid().toString()).child("savedjobs");
        mySavedJobsRef.addListenerForSingleValueEvent(
                new ToggleAddIDVEListener(ViewSingleJobActivity.this,jobPost.getPostid()));

    }
    public void requestJob(View v){
        DatabaseReference ref = database.getReference("Jobs").child(jobPost.getPostid()).child("requesters");
        ref.addListenerForSingleValueEvent(
                new ToggleAddIDVEListener(ViewSingleJobActivity.this,currentUser.getUid().toString()));
        ref = database.getReference("Users").child(currentUser.getUid()).child("requestedjobs");
        ref.addListenerForSingleValueEvent(
                new ToggleAddIDVEListener(ViewSingleJobActivity.this,jobPost.getPostid()));
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
            if (newDesc.equals("")) {
                dataSnapshot.getRef().addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this, "No description", newDesc));
            }
            if (!newTitle.equals("")) {
                dataSnapshot.getRef().addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this, "title", newTitle));
            }
            if (!newLoc.equals("")) {
                dataSnapshot.getRef().addListenerForSingleValueEvent(new ToggleAddIDVEListener(ViewSingleJobActivity.this, "location", newLoc));
            }
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {}
        });
    }
}
