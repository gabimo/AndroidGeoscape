package com.lawnscape;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileSettingsActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

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
                    startActivity(new Intent(ProfileSettingsActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //user is logged in
                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
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

    public void updateUserInfo(View v){
        // grab the widgets as objects
        int success = 0;
        TextView etName = (TextView) findViewById(R.id.etNameProfileSettings);
        TextView etLocation = (TextView) findViewById(R.id.etLocation);

        String newName = etName.getText().toString();
        String newLoc = etLocation.getText().toString();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Set name of user and location
        DatabaseReference myNameRef = database.getReference("Users/" + currentUser.getUid() + "/name");
        DatabaseReference myLocRef = database.getReference("Users/" + currentUser.getUid() + "/location");
        if(!newName.isEmpty()){
            myNameRef.setValue(newName);
        }
        if(!newLoc.isEmpty()) {
            myLocRef.setValue(newLoc);
        }
        finish();
    }
    public void backToProfile(View v){
        finish();
    }
    /******************* Menu Handling *******************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile_settings, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.profileSettingsMenu1:
                auth.signOut();
                return true;
            case R.id.profileSettingsMenu2:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
