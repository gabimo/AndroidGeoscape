package com.lawnscape;

import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.util.ArrayList;

public class ViewUserProfileActivity extends Activity {
    //userid the user wants to see
    private String userid;
    private FirebaseUser currentUser;
    private RatingBar rating;
    private ListView lvUserComments;
    private ArrayList<String> reviewsList;
    private StorageReference mStorageRef;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private ArrayAdapter<String> reviewAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);
        rating = (RatingBar) findViewById(R.id.ratingBarUser);
        lvUserComments = (ListView) findViewById(R.id.lvUserProfileComments);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        /*********************** IMPORTANT ****************************/
        userid = getIntent().getExtras().get("UserID").toString();
        userRef = database.getReference("Users").child(userid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView tvName = (TextView) findViewById(R.id.tvUserProfileName);
                TextView tvLoc = (TextView) findViewById(R.id.tvUserProfileLocation);
                tvName.setText(dataSnapshot.child("name").getValue().toString());
                tvLoc.setText(dataSnapshot.child("location").getValue().toString());
                /*
                * This is probably wrong, rating needs to be fixed
                 */
                if (dataSnapshot.hasChild("ratings")) {
                    float totalRating = (float) 0.0;
                    for (DataSnapshot userRating : dataSnapshot.child("ratings").getChildren()) {
                        totalRating += Float.valueOf(userRating.getValue().toString());
                        if (userRating.getKey().equals(currentUser.getUid().toString())) {
                            //make ratings a onetime thing totalRating = totalRating/dataSnapshot.child("ratings").getChildrenCount();
                            rating.setRating(totalRating);
                        }
                    }
                }
                //Display reviews
                if (dataSnapshot.hasChild("reviews")) {
                    reviewsList = new ArrayList<String>();
                    for (DataSnapshot review : dataSnapshot.child("reviews").getChildren()) {
                        reviewsList.add(review.getValue().toString());
                    }
                    reviewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(final RatingBar ratingBar, final float rating, boolean fromUser) {
                database.getReference("Users").child(userid).child("ratings").addListenerForSingleValueEvent(
                        new ToggleAddIDVEListener(ViewUserProfileActivity.this, currentUser.getUid(), String.valueOf(rating), false));
            }
        });
    }
    public void review(View v){
        EditText etReview = (EditText) findViewById(R.id.etUserReview);
        String newReview = etReview.getText().toString();
        userRef.child("reviews").addListenerForSingleValueEvent(new ToggleAddIDVEListener(this,currentUser.getUid().toString(),newReview));
    }
}
