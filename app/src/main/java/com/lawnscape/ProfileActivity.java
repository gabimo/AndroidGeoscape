package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

//Profile Activity
public class ProfileActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;

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
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    finish();
                }else{
                    //user is logged in
                    final TextView emailTV = (TextView) findViewById(R.id.tvUserEmail);
                    final TextView useridTV = (TextView) findViewById(R.id.tvUserID);
                    final TextView locationTV = (TextView) findViewById(R.id.tvLocationProfile);
                    final TextView nameTV = (TextView) findViewById(R.id.tvNameProfile);


                    emailTV.setText(user.getEmail().toString());
                    useridTV.setText(user.getUid().toString());

                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myNameRef = database.getReference("Users/"+user.getUid()+"/name");
                    myNameRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            String value = dataSnapshot.getValue(String.class);
                            nameTV.setText(value);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                        }
                    });
                    DatabaseReference myLocRef = database.getReference("Users/"+user.getUid()+"/location");
                    myLocRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // This method is called once with the initial value and again
                            // whenever data at this location is updated.
                            String value = dataSnapshot.getValue(String.class);
                            locationTV.setText(value);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                        }
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
    public void signout(View v){
        auth.signOut();
    }
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
                startActivity(new Intent(ProfileActivity.this, ProfileSettingsActivity.class));
                return true;
            case R.id.profileMenu2:
                startActivity(new Intent(ProfileActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.profileMenu3:
                auth.signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void gotoProfileSettings(View v){
        startActivity( new Intent( ProfileActivity.this, ProfileSettingsActivity.class));
    }
    public void gotoPostNewJob(View v){
        startActivity( new Intent( ProfileActivity.this, PostJobActivity.class));
    }
}
