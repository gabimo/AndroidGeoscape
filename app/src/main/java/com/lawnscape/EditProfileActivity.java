package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

public class EditProfileActivity extends Activity {

    private FirebaseDatabase database;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void updateUserInfo(View v){
        // grab the widgets as objects
        TextView etName = (TextView) findViewById(R.id.etEditProfileName);
        TextView etLocation = (TextView) findViewById(R.id.etEditProfileLocation);

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
        startActivity(new Intent(this, ViewMyProfileActivity.class));
    }
}