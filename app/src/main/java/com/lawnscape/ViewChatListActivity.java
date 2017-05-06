package com.lawnscape;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ViewChatListActivity extends AppCompatActivity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_all_chats);
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // user auth state is changed - user is not logged in
                    // launch login activity
                    startActivity(new Intent(ViewChatListActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //user is logged in
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    UserFragment f = (UserFragment) fm.findFragmentByTag("ChatListFragment");

                    if(f == null) {  // not added
                        f = new UserFragment();
                        ft.add(R.id.chatsFrameLayout, f, "ChatListFragment");
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

                    } else {  // already added

                        ft.remove(f);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
                    }

                    ft.commit();
                }
            }
        };
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Boiler plate Authentication
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Boiler plate Authentication
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    /*******************
     * Menu Handling
     *******************/
    //make the menu show up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_view_posts, menu);
        //WHile on the chat list activity replace the chat list icon with a link to view jobs
        menu.findItem(R.id.viewPostsMenuAllChats).setIcon(R.drawable.view_list_icon);
        menu.findItem(R.id.viewPostsMenuPostJob).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: 
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra("View", "all");
                if (upIntent != null && NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder builder = TaskStackBuilder.create(this);
                    builder.addNextIntentWithParentStack(upIntent);
                    builder.startActivities();
                } else {
                    if (upIntent != null) {
                        upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        this.startActivity(upIntent);
                    } else {
                        upIntent = new Intent( this, ViewJobsListsActivity.class);
                        upIntent.putExtra("View", "all");
                        startActivity(upIntent);
                    }
                }
                finish();
                return true;
        }
        return false;
    }

}