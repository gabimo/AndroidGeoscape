package com.lawnscape;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Mellis on 2/24/2017.
 */

public class UserListVEListener implements ValueEventListener {

    ArrayList<User> usersList;
    //Leave this alone to get all jobs
    ArrayList<String> usersToGet;
    UserListAdapter userAdapter;
    Context thisContext;

    public UserListVEListener(Context aCntxt, ArrayList<User> listofusers, ArrayList<String> desiredUsers, UserListAdapter userAdapt){
        usersList = listofusers;
        usersToGet = desiredUsers;
        userAdapter = userAdapt;
        thisContext = aCntxt;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot){
        usersList.clear();
        for (DataSnapshot jobNode : dataSnapshot.getChildren()) {
            //Either grabs all jobs, or the list of jobs passed via constructor
            if(usersToGet.contains(jobNode.getKey().toString())) {
                String title = (String) jobNode.child("name").getValue().toString();
                String location = (String) jobNode.child("location").getValue().toString();
                String userid = (String) jobNode.getKey().toString();
                //String postid = (String) jobNode.getKey().toString();
                usersList.add(new User(title, location, userid));
                //Tell the listview adaptor to update the listview based on the ArrayList updates
                userAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError firebaseError){
        // make a toast or something, thats why this class requires a context
    }

}
