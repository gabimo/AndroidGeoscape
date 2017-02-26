package com.lawnscape;

import android.os.Bundle;
import android.app.Activity;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewUserProfileActivity extends Activity {
    //userid the user wants to see
    private String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);
        /*********************** IMPORTANT ****************************/
        userid = getIntent().getExtras().get("UserID").toString();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = database.getReference("Users").child(userid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView tvName = (TextView) findViewById(R.id.tvUserProfileName);
                TextView tvLoc = (TextView) findViewById(R.id.tvUserProfileLocation);
                tvName.setText(dataSnapshot.child("name").getValue().toString());
                tvLoc.setText(dataSnapshot.child("location").getValue().toString());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
