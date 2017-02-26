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
                RatingBar rating = (RatingBar) findViewById(R.id.ratingBarUser);
                tvName.setText(dataSnapshot.child("name").getValue().toString());
                tvLoc.setText(dataSnapshot.child("location").getValue().toString());
                if(dataSnapshot.hasChild("rating")) {
                    rating.setRating(Float.valueOf(dataSnapshot.child("rating").getValue().toString()));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBarUser);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(final RatingBar ratingBar, final float rating, boolean fromUser) {
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("numratings")) {
                            Float totalStars = Float.valueOf(dataSnapshot.child("rating").getValue().toString());
                            int numRatings = Integer.valueOf(dataSnapshot.child("numratings").getValue().toString());
                            totalStars += rating;
                            numRatings += 1;
                            dataSnapshot.child("rating").getRef().setValue(totalStars);
                            dataSnapshot.child("numratings").getRef().setValue(numRatings);
                            ratingBar.setRating(totalStars/numRatings);
                        }else{
                            dataSnapshot.child("rating").getRef().setValue(rating);
                            dataSnapshot.child("numratings").getRef().setValue(1);
                            ratingBar.setRating(rating);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
    }
}
