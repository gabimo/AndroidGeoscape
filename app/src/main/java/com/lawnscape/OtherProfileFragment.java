package com.lawnscape;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OtherProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OtherProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OtherProfileFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    //userid the user wants to see
    private String userid;
    private FirebaseUser currentUser;
    private ListView lvUserReviews;
    private EditText etUserReview;
    private ArrayList<String> reviewList;
    private ArrayAdapter<String> reviewAdapter;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference userRef;
    private TextView tvName;
    private TextView tvLoc;
    private ImageView ivProfilePhoto;
    private RatingBar rating;
    // Create a storage reference from our app
    private FirebaseStorage storage;

    public OtherProfileFragment() {
        // Required empty public constructor
    }

    public static OtherProfileFragment newInstance(String userID) {
        OtherProfileFragment fragment = new OtherProfileFragment();
        Bundle args = new Bundle();
        args.putString("otherid", userID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userid = getArguments().getString("otherid");
        }

        userRef = database.getReference("Users").child(userid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                tvName.setText(dataSnapshot.child("name").getValue().toString());
                tvLoc.setText(dataSnapshot.child("location").getValue().toString());

                if (dataSnapshot.hasChild("reviews")) {
                    reviewList = new ArrayList<>();
                    reviewAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, reviewList);
                    for (DataSnapshot review : dataSnapshot.child("reviews").getChildren()) {
                        reviewList.add(review.getValue().toString());
                    }

                    lvUserReviews.setAdapter(reviewAdapter);
                    reviewAdapter.notifyDataSetChanged();
                }

            /*
            * This is probably wrong, rating needs to be fixed
             */
                if (dataSnapshot.hasChild("ratings")) {
                    float totalRating = (float) 0.0;
                    for (DataSnapshot userRating : dataSnapshot.child("ratings").getChildren()) {
                        totalRating += Float.valueOf(userRating.getValue().toString());
                    }
                    totalRating = totalRating / dataSnapshot.child("ratings").getChildrenCount();
                    rating.setRating(totalRating);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //profile photo
        storage = FirebaseStorage.getInstance();
        StorageReference pathReference = storage.getReference().child("userprofilephotos").child(userid);
        pathReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                // Pass it to Picasso to download, show in ImageView and caching
                Picasso.with(getContext()).load(uri.toString()).into(ivProfilePhoto);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //Default no image
                ivProfilePhoto.setImageDrawable(null);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_other_profile, container, false);
        rootView.findViewById(R.id.btnUserSubmitRating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child("ratings").addListenerForSingleValueEvent(
                        new ToggleAddIDVEListener(getContext(), currentUser.getUid(), String.valueOf(rating.getRating()),false));
                Toast.makeText(getContext(), R.string.rating_submitted, Toast.LENGTH_SHORT).show();
            }
        });
        rootView.findViewById(R.id.buttonUserProfileReview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRef.child("reviews").addListenerForSingleValueEvent(
                    new ToggleAddIDVEListener(getContext(), currentUser.getUid(), etUserReview.getText().toString(),false));
                Toast.makeText(getContext(), R.string.review_submitted, Toast.LENGTH_SHORT).show();
            }
        });
        tvName = (TextView) rootView.findViewById(R.id.tvUserProfileName);
        tvLoc = (TextView) rootView.findViewById(R.id.tvUserProfileLocation);
        etUserReview = (EditText) rootView.findViewById(R.id.etUserProfileReview);
        ivProfilePhoto = (ImageView) rootView.findViewById(R.id.ivUserProfileImage);
        rating = (RatingBar) rootView.findViewById(R.id.ratingBarUser);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        lvUserReviews = (ListView) rootView.findViewById(R.id.lvUserProfileReviews);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
