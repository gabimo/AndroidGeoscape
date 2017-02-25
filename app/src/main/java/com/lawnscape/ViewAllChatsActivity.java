package com.lawnscape;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewAllChatsActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();

    ArrayList<User> userList;
    ArrayList<String> useridList;
    UserListAdapter userAdapter;

    ListView userListView;


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
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is not logged in
                    // launch login activity
                    //startActivity(new Intent(JobListViewActivity.this, LoginActivity.class));
                    //finish();
                    System.out.println("LOG IN ERROR ");
                } else {
                    //user is logged in
                    currentUser = user;
                    DatabaseReference myChatsRef = database.getReference("Users").child(currentUser.getUid()).child("chatids");
                    //Gonna hold all the jobs, must init for adaptor
                    userList = new ArrayList<User>();
                    useridList = new ArrayList<String>();
                    //Put the jobs into the adaptor
                    //Find the listview widget and set up a connection to our ArrayList
                    userListView = (ListView) findViewById(R.id.lvJobRequesters);
                    userAdapter = new UserListAdapter(ViewAllChatsActivity.this, userList);
                    userListView.setAdapter(userAdapter);
                    myChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            DatabaseReference myUserRef = database.getReference("Users");
                            for (DataSnapshot curUserid : dataSnapshot.getChildren()) {
                                useridList.add(curUserid.getKey().toString());
                            }
                            myUserRef.addValueEventListener(
                                    new UserListVEListener(ViewAllChatsActivity.this, userList, useridList, userAdapter));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    if (!useridList.isEmpty()) {
                    }

                    //This handles clicks on individual user items from the list
                    // and bring you to a job specific chat page with the user
                    userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position,
                                                long id) {
                            User selectedUser = (User) userAdapter.getItem(position);
                            Intent singleJobViewIntent = new Intent(ViewAllChatsActivity.this, ViewSingleJobActivity.class);
                            Intent chatIntent = new Intent(ViewAllChatsActivity.this, ChatActivity.class);
                            chatIntent.putExtra("otherid", selectedUser.getUserid());
                            startActivity(chatIntent);
                            finish();
                        }
                    });
                }
            }
        };
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.viewPostsMenuMyProfile:
                startActivity(new Intent(ViewAllChatsActivity.this, ViewMyProfileActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuAllChats:
                startActivity(new Intent(ViewAllChatsActivity.this, ViewAllChatsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuMyJobs:
                startActivity(new Intent(ViewAllChatsActivity.this, ViewMyPostsActivity.class));
                finish();
                return true;
            case R.id.viewPostsMenuAllJobs:
                Intent allJobsViewIntent = new Intent(ViewAllChatsActivity.this, JobListViewActivity.class);
                allJobsViewIntent.putExtra("View", "all");
                startActivity(allJobsViewIntent);
                finish();
                return true;
            case R.id.viewPostsMenuSavedPosts:
                Intent savedJobsViewIntent = new Intent(ViewAllChatsActivity.this, JobListViewActivity.class);
                savedJobsViewIntent.putExtra("View", "saved");
                startActivity(savedJobsViewIntent);
                finish();
            case R.id.viewPostsMenuSignOut:
                auth.signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}