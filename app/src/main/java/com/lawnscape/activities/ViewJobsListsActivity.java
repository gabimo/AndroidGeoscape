package com.lawnscape.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lawnscape.fragments.JobListFragment;
import com.lawnscape.R;
import com.lawnscape.fragments.PostJobFragment;

public class ViewJobsListsActivity extends AppCompatActivity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private MenuItem postJobItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_job_lists);
        mAuth = FirebaseAuth.getInstance();
        TextView all = (TextView) findViewById(R.id.buttonViewAllJobs);
        TextView requested = (TextView) findViewById(R.id.buttonRequestedJobs);
        TextView active = (TextView) findViewById(R.id.buttonViewActiveJobsList);
        TextView saved = (TextView) findViewById(R.id.buttonViewSavedJobs);
        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listHandler(null);
            }
        });
        requested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listHandler("requestedjobs");
            }
        });
        saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listHandler("savedjobs");
            }
        });
        active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listHandler("activejobs");
            }
        });
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
                    Bundle extras = getIntent().getExtras();
                    if( extras != null && extras.containsKey("View") && !extras.getString("View").equals("all")){
                        listHandler(extras.getString("View"));
                    }else{
                        PostJobFragment f = (PostJobFragment) fm.findFragmentByTag("PostJobFrag");
                        JobListFragment g = null;
                        if (f != null) {
                            ft.replace(R.id.jobsListFrame, f, "PostJobFrag");
                        }else{
                            g = (JobListFragment) fm.findFragmentByTag("JobListFrag");
                            if(g == null){
                                g = new JobListFragment();
                            }
                            ft.replace(R.id.jobsListFrame, g, "JobListFrag");
                        }
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                        ft.disallowAddToBackStack();
                        ft.commit();
                    }
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

        /************* End LifeCycle ****************/
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
                    PostJobFragment f = new PostJobFragment();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.jobsListFrame, f, "PostJobFrag");
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                    postJobItem = item;
                    item.setVisible(false);
                    return true;
                case R.id.viewPostsMenuMyProfile:
                    startActivity( new Intent( ViewJobsListsActivity.this, ViewProfileActivity.class));
                    return true;
                case R.id.viewPostsMenuAllChats:
                    startActivity(new Intent(ViewJobsListsActivity.this, ViewChatListActivity.class));
                    finish();
                    return true;
                case R.id.viewPostsMenuMyJobs:
                    listHandler("myjobs");
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
    private void listHandler(String list){
        JobListFragment f = new JobListFragment();

        Bundle args = new Bundle();
        args.putString("jobset", list);
        f.setArguments(args);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.jobsListFrame, f, "JobListFrag");
        fragmentTransaction.disallowAddToBackStack();
        fragmentTransaction.commit();
    }
/********************* Switch to map view ********************/
    @SuppressWarnings("UnusedParameters")
    public void showMapOfJobs(View v){
        Intent MapJobsViewIntent = new Intent(this, MapJobsActivity.class);
        startActivity(MapJobsViewIntent);
    }
}