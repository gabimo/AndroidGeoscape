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

    public ToggleAddIDVEListener(Context activityContext, String desiredID){
        id = desiredID;
        currentActivity = activityContext;
        key = "NOKEY";
    }

    public ToggleAddIDVEListener(Context activityContext, String keyVal, String desiredID){
        currentActivity = activityContext;
        id = desiredID;
        key = keyVal;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        boolean isDuplicate = false;
        for (DataSnapshot node : dataSnapshot.getChildren()) {
            if (node.getValue().toString().equals(id)) {
                isDuplicate = true;
                //Remove on reclick
                node.getRef().removeValue();
            }
        }
        if (!isDuplicate) {
            if(key.equals("NOKEY")) {
                dataSnapshot.getRef().push().setValue(id);
            }else{
                dataSnapshot.child(key).getRef().setValue(id);
            }
        }
    }
    @Override
    public void onCancelled(DatabaseError databaseError) {
        Toast.makeText(currentActivity ,"DB ERROR: Could not do it",Toast.LENGTH_SHORT).show();
    }
}