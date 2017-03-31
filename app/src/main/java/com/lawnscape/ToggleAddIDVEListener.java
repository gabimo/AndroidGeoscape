package com.lawnscape;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Mellis on 2/23/2017.
 *
 * Given a reference such as "Users/myUserIDxvHDJdk/savedjobs/" it will find the given ID
 * if it is in the given tree and remove it, or, if it cannot find the given ID it will add it
 *
 * Basically it can add jobs to a users savedjobs list, or add users to some job's list of users
 * that requested to do the job in a toggle fashion
 *
 */

public class ToggleAddIDVEListener implements ValueEventListener {
    String id;
    String key;
    Context currentActivity;
    boolean willRemove = true;

    public ToggleAddIDVEListener(Context activityContext, String desiredID){
        id = desiredID;
        currentActivity = activityContext;
        key = "";
    }
    public ToggleAddIDVEListener(Context activityContext, String keyVal, String desiredID){
        currentActivity = activityContext;
        id = desiredID;
        key = keyVal;
    }
    public ToggleAddIDVEListener(Context activityContext, String keyVal, String desiredID, boolean shouldRemove){
        currentActivity = activityContext;
        id = desiredID;
        key = keyVal;
        willRemove = shouldRemove;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        boolean isDuplicate = false;
        for (DataSnapshot node : dataSnapshot.getChildren()) {
            /*
            * If anything causes logic errors I bet it will be the following if statement
             */
            if (node.getValue().toString().equals(id)&&willRemove||node.getKey().equals(id)) {
                isDuplicate = true;
                //Remove on reclick
                if(willRemove) {
                    node.getRef().removeValue();
                }
            }
        }
        if (!isDuplicate) {
            if(key.equals("")) {
                dataSnapshot.getRef().push().setValue(id);
            }else{
                dataSnapshot.child(key).getRef().setValue(id);
            }
        }
    }
    @Override
    public void onCancelled(DatabaseError databaseError) {
        Toast.makeText(currentActivity ,"DB ERROR: Could not add or remove",Toast.LENGTH_SHORT).show();
    }
}