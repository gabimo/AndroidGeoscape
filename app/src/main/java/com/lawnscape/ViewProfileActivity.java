package com.lawnscape;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//Profile Activity
public class ViewProfileActivity extends AppCompatActivity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        //get firebase auth instance
        mAuth = FirebaseAuth.getInstance();
        //This mAuthListener will be called every time that the activity runs onStart()
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // User needs to sign in
                    startActivity(new Intent(ViewProfileActivity.this, LoginActivity.class));
                    finish();
                }else{
                    //user is logged in
                    /*********************** IMPORTANT ****************************/
                    try {
                        userid = getIntent().getExtras().get("UserID").toString();
                    }catch (Exception e){
                        //programmer error
                    }
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    if(userid == null) {
                        MyProfileFragment myProfileFrag = (MyProfileFragment) fm.findFragmentByTag("MyProfileFrag");
                        if (myProfileFrag == null) {  // not added
                            myProfileFrag = new MyProfileFragment();
                            ft.add(R.id.profileFrame, myProfileFrag, "MyProfileFrag");
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        }
                    }else{
                        OtherProfileFragment otherProfileFrag = (OtherProfileFragment) fm.findFragmentByTag("OtherProfileFrag");
                        Bundle args = new Bundle();
                        args.putString("otherid", userid);
                        if (otherProfileFrag == null) {  // not added
                            otherProfileFrag = new OtherProfileFragment();
                            otherProfileFrag.setArguments(args);
                            ft.add(R.id.profileFrame, otherProfileFrag, "OtherProfileFrag");
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        }
                    }
                    ft.commit();
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
                finish();
                return true;
        }
    }
}
