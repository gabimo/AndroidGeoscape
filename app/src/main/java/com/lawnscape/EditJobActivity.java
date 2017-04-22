package com.lawnscape;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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

public class EditJobActivity extends AppCompatActivity {

    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser currentUser;
    private Job jobPost;

    private EditText etTitle;
    private EditText etLocation;
    private EditText etDesc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_job);
        mAuth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(EditJobActivity.this, LoginActivity.class));
                    finish();
                } else {
                    storage = FirebaseStorage.getInstance();
                    database = FirebaseDatabase.getInstance();
                    Intent jobIntent = getIntent();
                    jobPost = jobIntent.getParcelableExtra("Job");
                    etTitle = (EditText) findViewById(R.id.etEditJobTitle);
                    etLocation = (EditText) findViewById(R.id.etEditJobLocation);
                    etDesc = (EditText) findViewById(R.id.etEditJobDescription);
                    etTitle.setText(jobPost.getTitle());
                    etLocation.setText(jobPost.getLocation());
                    etDesc.setText(jobPost.getDescription());

                    //This finds the photo data by the job id from firebase storage, nothing is passed around
                    StorageReference jobPhotoRef = storage.getReference().child("jobphotos").child(jobPost.getPostid()).child("mainphoto ");
                    jobPhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            // Pass it to Picasso to download, show in ImageView and caching
                            //Picasso.with(EditJobActivity.this).load(uri.toString()).into(ivPhoto);
                        }
                    });
                    DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("Admins");
                    adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(currentUser.getUid().toString())) {
                                //is an admin
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
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

    public void postChanges(View v) {
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
                    newDesc = "No description";
                }
                dataSnapshot.getRef().addListenerForSingleValueEvent(new ToggleAddIDVEListener(EditJobActivity.this, "description", newDesc));
                if (!newTitle.equals("")) {
                    dataSnapshot.getRef().addListenerForSingleValueEvent(new ToggleAddIDVEListener(EditJobActivity.this, "title", newTitle));
                }
                if (!newLoc.equals("")) {
                    dataSnapshot.getRef().addListenerForSingleValueEvent(new ToggleAddIDVEListener(EditJobActivity.this, "location", newLoc));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
