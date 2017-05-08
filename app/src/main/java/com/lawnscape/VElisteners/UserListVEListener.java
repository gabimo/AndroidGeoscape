package com.lawnscape.VElisteners;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.lawnscape.adapters.UserListAdapter;
import com.lawnscape.classes.User;

import java.util.ArrayList;

public class UserListVEListener implements ValueEventListener {

    private final ArrayList<User> usersList;
    //Leave this alone to get all jobs
    private final ArrayList<String> usersToGet;
    private final UserListAdapter userAdapter;

    public UserListVEListener(ArrayList<User> listofusers, ArrayList<String> desiredUsers, UserListAdapter userAdapt){
        usersList = listofusers;
        usersToGet = desiredUsers;
        userAdapter = userAdapt;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot){
        usersList.clear();
        for (DataSnapshot userNode : dataSnapshot.getChildren()) {
            //Either grabs all jobs, or the list of jobs passed via constructor
            if(usersToGet.contains(userNode.getKey())) {
                if(userNode.hasChild("name")&&userNode.hasChild("location")) {
                    usersList.add(new User(
                            userNode.child("name").getValue().toString(),
                            userNode.child("location").getValue().toString(),
                            userNode.getKey()));
                    //Tell the listview adaptor to update the listview based on the ArrayList updates
                    userAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError firebaseError){
        // make a toast or something, thats why this class requires a context
    }

}
