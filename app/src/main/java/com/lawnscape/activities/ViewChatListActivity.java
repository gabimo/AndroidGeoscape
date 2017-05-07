package com.lawnscape.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lawnscape.fragments.ChatListFragment;
import com.lawnscape.R;

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
                    ChatListFragment f = (ChatListFragment) fm.findFragmentByTag("ChatListFragment");

                    if(f == null) {  // not added
                        f = new ChatListFragment();
                        ft.add(R.id.chatsFrameLayout, f, "ChatListFragment");
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

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



}