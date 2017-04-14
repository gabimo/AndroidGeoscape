package com.lawnscape;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

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

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser currentUser;
    //photo vars
    private final int PICK_PHOTO_FROM_GALLERY = 25;
    private ImageView ivProfileImage;
    private Uri imageUri;
    private DatabaseReference usersRef;
    // grab the widgets as objects
    private EditText etName;
    private EditText etLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
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
                    startActivity(new Intent(EditProfileActivity.this, LoginActivity.class));
                    finish();
                } else {
                    etName = (EditText) findViewById(R.id.etEditProfileName);
                    etLocation = (EditText) findViewById(R.id.etEditProfileLocation);
                    storage = FirebaseStorage.getInstance();
                    database = FirebaseDatabase.getInstance();
                    usersRef = database.getReference("Users").child(currentUser.getUid().toString());
                    usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            etName.setText(dataSnapshot.child("name").getValue().toString());
                            etLocation.setText(dataSnapshot.child("location").getValue().toString());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    ivProfileImage = (ImageView) findViewById(R.id.ivEditProfile);

                    //This finds the photo data by the job id from firebase storage, nothing is passed around
                    StorageReference jobPhotoRef = storage.getReference().child("userprofilephotos").child(currentUser.getUid());
                    jobPhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            // Pass it to Picasso to download, show in ImageView and caching
                            Picasso.with(EditProfileActivity.this).load(uri.toString()).into(ivProfileImage);
                        }
                    });
                    ivProfileImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent photoGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
                            photoGalleryIntent.setType("image/*");
                            //photoGalleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            startActivityForResult(photoGalleryIntent, PICK_PHOTO_FROM_GALLERY);
                        }
                    });
                }
            }
        };
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    public void updateUserInfo(View v) {
        String newName = etName.getText().toString();
        String newLoc = etLocation.getText().toString();

        // Set name of user and location
        if (!newName.isEmpty() && !newLoc.isEmpty()) {
            usersRef.setValue(new User(newName, newLoc));
        }
        if (ivProfileImage.getDrawable() != null) {
            StorageReference pathReference = storage.getReference().child("userprofilephotos").child(currentUser.getUid());
            pathReference.putFile(imageUri);
        }
        startActivity(new Intent(this, ViewMyProfileActivity.class));
        finish();
    }

    public void backToProfile(View v) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri targetURI = data.getData();
            ivProfileImage.setImageURI(targetURI);
            imageUri = targetURI;
        }
    }
}