package com.lawnscape;

import android.os.Bundle;
import android.app.Activity;
import android.widget.RatingBar;
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
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        /*********************** IMPORTANT ****************************/
        userid = getIntent().getExtras().get("UserID").toString();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference userRef = database.getReference("Users").child(userid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView tvName = (TextView) findViewById(R.id.tvUserProfileName);
                TextView tvLoc = (TextView) findViewById(R.id.tvUserProfileLocation);
                RatingBar rating = (RatingBar) findViewById(R.id.ratingBarUser);
                tvName.setText(dataSnapshot.child("name").getValue().toString());
                tvLoc.setText(dataSnapshot.child("location").getValue().toString());
                /*
                * This is probably wrong, rating needs to be fixed
                 */
                if(dataSnapshot.hasChild("ratings")) {
                    float totalRating = (float)0.0;
                    for(DataSnapshot userRating: dataSnapshot.child("ratings").getChildren()){
                        totalRating += Float.valueOf(userRating.getValue().toString());
                        if (userRating.getKey().equals(currentUser.getUid().toString())){
                            //rating.setIsIndicator(true);
                        }
                    }
                    totalRating = totalRating/dataSnapshot.child("ratings").getChildrenCount();
                    rating.setRating(totalRating);
                    rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                        @Override
                        public void onRatingChanged(final RatingBar ratingBar, final float rating, boolean fromUser) {
                            database.getReference("Users").child(userid).child("ratings").addListenerForSingleValueEvent(
                                    new ToggleAddIDVEListener(ViewUserProfileActivity.this, currentUser.getUid(), String.valueOf(rating), false));
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
