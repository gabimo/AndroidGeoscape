package com.lawnscape;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewUserProfileActivity extends Activity {
    //userid the user wants to see
    private String userid;
    private FirebaseUser currentUser;
    private ListView lvUserReviews;
    private EditText etUserReview;
    private ArrayList<String> reviewList;
    private ArrayAdapter<String> reviewAdapter;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private TextView tvName;
    private TextView tvLoc;
    private ImageView ivProfilePhoto;
    // Create a storage reference from our app
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);
        tvName = (TextView) findViewById(R.id.tvUserProfileName);
        tvLoc = (TextView) findViewById(R.id.tvUserProfileLocation);
        etUserReview = (EditText) findViewById(R.id.etUserProfileReview);
        ivProfilePhoto = (ImageView) findViewById(R.id.ivUserProfileImage);
        storage = FirebaseStorage.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        /*********************** IMPORTANT ****************************/
        userid = getIntent().getExtras().get("UserID").toString();
        userRef = database.getReference("Users").child(userid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                RatingBar rating = (RatingBar) findViewById(R.id.ratingBarUser);
                tvName.setText(dataSnapshot.child("name").getValue().toString());
                tvLoc.setText(dataSnapshot.child("location").getValue().toString());

                if(dataSnapshot.hasChild("reviews")) {
                    lvUserReviews = (ListView) findViewById(R.id.lvUserProfileReviews);
                    reviewList = new ArrayList<>();
                    reviewAdapter = new ArrayAdapter<String>(ViewUserProfileActivity.this, android.R.layout.simple_list_item_1, reviewList);
                    for(DataSnapshot review: dataSnapshot.child("reviews").getChildren()){
                        reviewList.add(review.getValue().toString());
                    }
                    lvUserReviews.setAdapter(reviewAdapter);
                    reviewAdapter.notifyDataSetChanged();
                }

                /*
                * This is probably wrong, rating needs to be fixed
                 */
                if(dataSnapshot.hasChild("ratings")) {
                    float totalRating = (float)0.0;
                    for(DataSnapshot userRating: dataSnapshot.child("ratings").getChildren()){
                        totalRating += Float.valueOf(userRating.getValue().toString());
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
        //profile photo
        StorageReference pathReference = storage.getReference().child("userprofilephotos").child(userid);
        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                // Pass it to Picasso to download, show in ImageView and caching
                Picasso.with(ViewUserProfileActivity.this).load(uri.toString()).into(ivProfilePhoto);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Default no image
                ivProfilePhoto.setImageDrawable(null);
            }
        });
    }
    public void reviewUser(View v){
        userRef.child("reviews").addListenerForSingleValueEvent(
                new ToggleAddIDVEListener(this, currentUser.getUid(), etUserReview.getText().toString()));
        Toast.makeText(this, "Your review has been submitted, to edit submit a new review",Toast.LENGTH_SHORT).show();
    }
}
