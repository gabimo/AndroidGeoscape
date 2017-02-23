package com.lawnscape;

import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Mellis on 2/22/2017.
 */

public class RequestVEListener implements ValueEventListener {
    private String postid;
    private String currentuserid;

    public RequestVEListener(String desiredPostID, String currUserID){
        postid = desiredPostID;
        currentuserid = currUserID;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

        for (DataSnapshot jobNode : dataSnapshot.getChildren()) {

            String keyVal = jobNode.child("postid").getValue().toString();

            //find the right job
            if (keyVal.equals(postid)) {
                boolean isDuplicate = false;
                for(DataSnapshot requesterid : jobNode.child("requesters").getChildren()){
                    if( requesterid.getValue().toString().equals(currentuserid)){
                        isDuplicate = true;
                        //remove on reclick
                        requesterid.getRef().removeValue();
                    }
                }
                if(!isDuplicate){
                    jobNode.child("requesters").getRef().push().setValue(currentuserid);
                }
            }
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }
}
