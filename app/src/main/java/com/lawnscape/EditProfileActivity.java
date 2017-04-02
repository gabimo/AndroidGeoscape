package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class EditProfileActivity extends Activity {

    private FirebaseDatabase database;
    private FirebaseUser currentUser;
    //photo vars
    private GridView gvUploadPhotos;
    private ArrayList<Uri> uriList;
    private PhotoAdapter photoAdapter;
    private final int PICK_PHOTO_FROM_GALLERY = 5;
    //Location Vars
    private Location myCurLoc;
    private final int PERMISSION_ACCESS_COARSE_LOCATION = 1;// no reason, just a 16 bit number

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void updateUserInfo(View v){
        // grab the widgets as objects
        EditText etName = (EditText) findViewById(R.id.etEditProfileName);
        EditText etLocation = (EditText) findViewById(R.id.etEditProfileLocation);

        String newName = etName.getText().toString();
        String newLoc = etLocation.getText().toString();

        // Set name of user and location
        DatabaseReference usersRef = database.getReference("Users").child(currentUser.getUid().toString());
        if(!newName.isEmpty()&&!newLoc.isEmpty()){
            DatabaseReference newUserRef = usersRef;
            newUserRef.setValue(new User(newName, newLoc));
        }
        finish();
    }
    public void backToProfile(View v){
        finish();
    }

    public void pickImage(View v){
        Intent photoGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
        photoGalleryIntent.setType("image/*");
        //photoGalleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(photoGalleryIntent, PICK_PHOTO_FROM_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri targetURI = data.getData();
            if(!uriList.isEmpty()){
                uriList.clear();
            }
            uriList.add(targetURI);
            photoAdapter.notifyDataSetChanged();
        }
    }
}