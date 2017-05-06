package com.lawnscape;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewJobsListsActivity extends AppCompatActivity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_job_lists);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // launch login activity if user has no account
                    startActivity(new Intent(ViewJobsListsActivity.this, LoginActivity.class));
                    finish();
                }else{
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    JobListFragment f = (JobListFragment) fm.findFragmentByTag("JobListFragment");

                    if(f == null) {  // not added
                        f = new JobListFragment();
                        ft.add(R.id.jobsListFrame, f, "JobListFragment");
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

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

    /************** End LifeCycle ****************/
    /******************* Menu Handling *******************/
    //make the menu show up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Creates the top action icons and the top menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_posts, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles item selection from the action bar and menu
        switch (item.getItemId()) {
            case R.id.viewPostsMenuPostJob:
                startActivity(new Intent(this, PostJobActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuMyProfile:
                startActivity( new Intent( ViewJobsListsActivity.this, ViewMyProfileActivity.class));
                return true;
            case R.id.viewPostsMenuAllChats:
                startActivity(new Intent(ViewJobsListsActivity.this, ViewChatListActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuMyJobs:
                startActivity(new Intent(ViewJobsListsActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuAllJobs:
                if(getIntent().getExtras().get("View").toString().equals("saved")) {
                    getIntent().removeExtra("View");
                    //getIntent().putExtra("View", "all");
                }
                recreate();
                return true;
            case R.id.viewPostsMenuSearch:
                Intent SearchIntent = new Intent(this, SearchActivity.class);
                startActivity(SearchIntent);
                return true;
            case R.id.viewPostsMenuJobsMap:
                Intent MapAllJobsViewIntent = new Intent(this, MapJobsActivity.class);
                startActivity(MapAllJobsViewIntent);
                return true;
            case R.id.viewPostsMenuSignOut:
                mAuth.signOut();
                startActivity(new Intent(ViewJobsListsActivity.this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

/********************* Switch to map view ********************/
    public void showMapOfJobs(View v){
        Intent MapJobsViewIntent = new Intent(this, MapJobsActivity.class);
        startActivity(MapJobsViewIntent);
    }
}