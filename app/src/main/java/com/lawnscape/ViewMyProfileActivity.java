package com.lawnscape;

import android.support.v4.app.FragmentActivity;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//Profile Activity
public class ViewMyProfileActivity extends FragmentActivity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private FirebaseDatabase database;
    /************** Begin LifeCycle Functions ****************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_profile);
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
                    startActivity(new Intent(ViewMyProfileActivity.this, LoginActivity.class));
                    finish();
                }else{
                    setContentView(R.layout.activity_view_my_profile);
                    //user is logged in
                    database = FirebaseDatabase.getInstance ();
                    final TextView emailTV = (TextView) findViewById(R.id.tvMyProfileUserEmail);
                    final TextView useridTV = (TextView) findViewById(R.id.tvMyProfileUserID);
                    final TextView locationTV = (TextView) findViewById(R.id.tvMyProfileLocation);
                    final TextView nameTV = (TextView) findViewById(R.id.tvMyProfileName);
                    emailTV.setText(currentUser.getEmail().toString());
                    useridTV.setText(currentUser.getUid().toString());

                    database.getReference("Users").child(currentUser.getUid()).child("name")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
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
                    database.getReference("Users").child(currentUser.getUid()).child("location")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
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
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
             mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    /************** End LifeCycle ****************/
    public void gotoProfileSettings(View v){
        startActivity(new Intent(this,EditProfileActivity.class));
    }
    public void gotoPostNewJob(View v){
        startActivity( new Intent( ViewMyProfileActivity.this, PostJobActivity.class));
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
            case R.id.profileMenuSettings:
                setContentView(R.layout.activity_profile_settings);
                return true;
            case R.id.profileMenuChats:
                startActivity(new Intent(ViewMyProfileActivity.this, ViewActiveChatsActivity.class));
                return true;
            case R.id.profileMenuSearch:
                startActivity(new Intent(ViewMyProfileActivity.this, SearchActivity.class));
                return true;
            case R.id.profileMenuMyJobs:
                startActivity(new Intent(ViewMyProfileActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.profileMenuAllJobs:
                Intent allJobsViewIntent = new Intent(ViewMyProfileActivity.this, ViewJobsListsActivity.class);
                allJobsViewIntent.putExtra("View", "all");
                startActivity(allJobsViewIntent);
                finish();
                return true;
            case R.id.profileMenuAllJobsMap:
                Intent MapAllJobsViewIntent = new Intent(ViewMyProfileActivity.this, MapJobsActivity.class);
                startActivity(MapAllJobsViewIntent);
                return true;
            case R.id.profileMenuSignOut:
                mAuth.signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void searchJobsButton(View v){
        startActivity(new Intent(ViewMyProfileActivity.this, SearchActivity.class));
    }
    public void listAllJobsButton(View v){
        Intent allJobsViewIntent = new Intent(ViewMyProfileActivity.this, ViewJobsListsActivity.class);
        allJobsViewIntent.putExtra("View", "all");
        startActivity(allJobsViewIntent);
        finish();
    }
    public void mapJobsButton(View v){
        Intent MapAllJobsViewIntent = new Intent(ViewMyProfileActivity.this, MapJobsActivity.class);
        startActivity(MapAllJobsViewIntent);
    }
    public void myPostsButton(View v){
        startActivity(new Intent(ViewMyProfileActivity.this, ViewMyPostsActivity.class));
        finish();
    }
}
