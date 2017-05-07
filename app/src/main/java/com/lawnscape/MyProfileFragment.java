package com.lawnscape;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyProfileFragment extends Fragment {
    private FirebaseUser currentUser;

    private FirebaseStorage storage;

    private TextView tvEmail;
    private TextView tvUserID;
    private TextView tvLocation;
    private TextView tvName;

    private ImageView ivProfilePhoto;

    private FirebaseDatabase database;

    private OnFragmentInteractionListener mListener;

    public MyProfileFragment() {
        // Required empty public constructor
    }

    public static MyProfileFragment newInstance() {
        MyProfileFragment fragment = new MyProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance ();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_profile, container, false);

        tvEmail = (TextView) rootView.findViewById(R.id.tvMyProfileUserEmail);
        tvUserID = (TextView) rootView.findViewById(R.id.tvMyProfileUserID);
        tvLocation = (TextView) rootView.findViewById(R.id.tvMyProfileLocation);
        tvName = (TextView) rootView.findViewById(R.id.tvMyProfileName);
        ivProfilePhoto = (ImageView) rootView.findViewById(R.id.ivMyProfileImage);

        tvEmail.setText(currentUser.getEmail().toString());
        tvUserID.setText(currentUser.getUid().toString());
        ivProfilePhoto.setImageDrawable(null);

        //This finds and displays the photo data by the user id from firebase storage
        StorageReference jobPhotoRef = storage.getReference().child("userprofilephotos").child(currentUser.getUid());
        jobPhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Pass it to Picasso to download, show in ImageView and caching
                Picasso.with(getContext()).load(uri.toString()).into(ivProfilePhoto);
            }
        });

        //This finds and displays the users name and location
        database.getReference("Users").child(currentUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild("name")) {
                            tvName.setText(dataSnapshot.child("name").getValue().toString());
                        }
                        if(dataSnapshot.hasChild("location")) {
                            tvLocation.setText(dataSnapshot.child("location").getValue().toString());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError error) { }
                });
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
        void onFragmentInteraction(Uri uri);
    }
}
