package com.lawnscape;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.squareup.picasso.Picasso;

//Profile Activity
public class ViewMyProfileActivity extends AppCompatActivity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    private FirebaseUser currentUser;

    private FirebaseStorage storage;

    private TextView tvEmail;
    private TextView tvUserID;
    private TextView tvLocation;
    private TextView tvName;

    private ImageView ivProfilePhoto;

    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_profile);
        //get firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        //This mAuthListener will be called every time that the activity runs onStart()
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                /*
                The following code will be ran when onStart runs in the Android Lifecycle
                 */
                //This finds the current phone's login info, if any
                currentUser = firebaseAuth.getCurrentUser();
                //currentUser will be null if the user has not logged in(or logged out)
                if (currentUser == null) {
                    // User needs to sign in
                    startActivity(new Intent(ViewMyProfileActivity.this, LoginActivity.class));
                    finish();
                }else{
                    //user is logged in
                    storage = FirebaseStorage.getInstance();
                    database = FirebaseDatabase.getInstance ();

                    tvEmail = (TextView) findViewById(R.id.tvMyProfileUserEmail);
                    tvUserID = (TextView) findViewById(R.id.tvMyProfileUserID);
                    tvLocation = (TextView) findViewById(R.id.tvMyProfileLocation);
                    tvName = (TextView) findViewById(R.id.tvMyProfileName);
                    ivProfilePhoto = (ImageView) findViewById(R.id.ivMyProfileImage);

                    tvEmail.setText(currentUser.getEmail().toString());
                    tvUserID.setText(currentUser.getUid().toString());
                    ivProfilePhoto.setImageDrawable(null);

                    //This finds and displays the photo data by the user id from firebase storage
                    StorageReference jobPhotoRef = storage.getReference().child("userprofilephotos").child(currentUser.getUid());
                    jobPhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Pass it to Picasso to download, show in ImageView and caching
                            Picasso.with(ViewMyProfileActivity.this).load(uri.toString()).into(ivProfilePhoto);
                        }
                    });

                    //This finds and displays the users name and location
                    database.getReference("Users").child(currentUser.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("name")) {
                                tvName.setText(dataSnapshot.child("name").getValue().toString());
                            }
                            if(dataSnapshot.hasChild("location")) {
                                tvLocation.setText(dataSnapshot.child("location").getValue().toString());
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError error) { }
                    });
                }
            }
        };
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //This stuff just draws the menu buttons
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection of the
        switch (item.getItemId()) {
            case R.id.profileMenuSettings:
                startActivity(new Intent(this,EditProfileActivity.class));
                finish();
                return true;
            case R.id.profileMenuChats:
                startActivity(new Intent(this, ViewChatListActivity.class));
                return true;
            case R.id.profileMenuSearch:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.profileMenuMyJobs:
                startActivity(new Intent(this, ViewMyPostsActivity.class));
                return true;
            case R.id.profileMenuAllJobs:
                Intent allJobsViewIntent = new Intent(this, ViewJobsListsActivity.class);
                allJobsViewIntent.putExtra("View", "all");
                startActivity(allJobsViewIntent);
                return true;
            case R.id.profileMenuAllJobsMap:
                Intent MapAllJobsViewIntent = new Intent(this, MapJobsActivity.class);
                startActivity(MapAllJobsViewIntent);
                return true;
            case R.id.profileMenuSignOut:
                mAuth.signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /*
    Not necessary, these are just placeholding button actions on the profile activity
     */
}
