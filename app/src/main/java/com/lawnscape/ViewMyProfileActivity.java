package com.lawnscape;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

//Profile Activity
public class ViewMyProfileActivity extends FragmentActivity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private FirebaseDatabase database;
    private StorageReference mStorageRef;
    /************** Begin LifeCycle Functions ****************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ViewMyProfileActivity.this, LoginActivity.class));
                    finish();
                }else{
                    setContentView(R.layout.activity_profile);
                    //user is logged in
                    database = FirebaseDatabase.getInstance();
                    //file storage uri/objects are not the same as database storage uri/objects
                    /*
                    mStorageRef = FirebaseStorage.getInstance().getReference("UserProfileImages").child(currentUser.getUid());
                    mStorageRef.getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    try {
                                        Picasso.with(ViewMyProfileActivity.this).load(uri.toString()).resize(190, 150).into((ImageView) findViewById(R.id.ivProfileImage));
                                    }catch (Exception e){

                                    }
                                }
                            }).addOnFailureListener(ViewMyProfileActivity.this, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Fail");
                                }
                            });
*/
                    final TextView emailTV = (TextView) findViewById(R.id.tvUserEmail);
                    final TextView useridTV = (TextView) findViewById(R.id.tvUserID);
                    final TextView locationTV = (TextView) findViewById(R.id.tvLocationProfile);
                    final TextView nameTV = (TextView) findViewById(R.id.tvNameProfile);
                    //image API for android
                    Picasso.with(ViewMyProfileActivity.this);
                    emailTV.setText(currentUser.getEmail().toString());
                    useridTV.setText(currentUser.getUid().toString());

                    DatabaseReference myNameRef = database
                            .getReference("Users/"+currentUser.getUid()+"/name");
                    myNameRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            String value = dataSnapshot.getValue(String.class);
                            nameTV.setText(value);
                        }
                        @Override
                        public void onCancelled(DatabaseError error) { }
                    });
                    DatabaseReference myLocRef = database
                            .getReference("Users/"+currentUser.getUid()+"/location");
                    myLocRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            String value = dataSnapshot.getValue(String.class);
                            locationTV.setText(value);
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
        inflater.inflate(R.menu.menu_profile, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.profileMenu1:
                //setContentView(R.layout.activity_profile_settings);
                startActivity(new Intent(this, UploadPhotosActivity.class));
                return true;
            case R.id.profileMenu2:
                startActivity(new Intent(ViewMyProfileActivity.this, ViewActiveChatsActivity.class));
                return true;
            case R.id.profileMenuSearch:
                startActivity(new Intent(ViewMyProfileActivity.this, SearchActivity.class));
                return true;
            case R.id.profileMenu3:
                startActivity(new Intent(ViewMyProfileActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.profileMenu4:
                Intent allJobsViewIntent = new Intent(ViewMyProfileActivity.this, ViewJobsListsActivity.class);
                allJobsViewIntent.putExtra("View", "all");
                startActivity(allJobsViewIntent);
                finish();
                return true;
            case R.id.profileMenu5:
                Intent MapAllJobsViewIntent = new Intent(ViewMyProfileActivity.this, MapJobsActivity.class);
                startActivity(MapAllJobsViewIntent);
                return true;
            case R.id.profileMenu6:
                auth.signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void gotoProfileSettings(View v){
        setContentView(R.layout.activity_profile_settings);
    }
    public void gotoPostNewJob(View v){
        startActivity( new Intent( ViewMyProfileActivity.this, PostJobActivity.class));
    }
    public void updateUserInfo(View v){
        // grab the widgets as objects
        int success = 0;
        TextView etName = (TextView) findViewById(R.id.etProfSettingsName);
        TextView etLocation = (TextView) findViewById(R.id.etProfSettingsLocation);

        String newName = etName.getText().toString();
        String newLoc = etLocation.getText().toString();

        // Set name of user and location
        DatabaseReference usersRef = database.getReference("Users").child(currentUser.getUid().toString());
        if(!newName.isEmpty()&&!newLoc.isEmpty()){
            DatabaseReference newUserRef = usersRef;
            newUserRef.setValue(new User(newName, newLoc));
        }
        //"return" to profile activity
        recreate();
    }
    public void backToProfile(View v){
        setContentView(R.layout.activity_profile);
    }
    public void uploadPhoto(View v){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);//one can be replaced with any action code
    }
}
