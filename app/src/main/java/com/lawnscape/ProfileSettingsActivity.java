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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        TextView etName = (TextView) findViewById(R.id.etProfSettingsName);
        TextView etLocation = (TextView) findViewById(R.id.etProfSettingsLocation);

        String newName = etName.getText().toString();
        String newLoc = etLocation.getText().toString();

        // Set name of user and location
        FirebaseDatabase database = FirebaseDatabase.getInstance();

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
