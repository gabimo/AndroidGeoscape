package com.lawnscape.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

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
import com.lawnscape.activities.ViewProfileActivity;
import com.lawnscape.classes.User;
import com.lawnscape.R;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends Fragment {

    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser currentUser;
    //photo vars
    private final int PICK_PHOTO_FROM_GALLERY = 25;
    private ImageView ivProfileImage;
    private Uri imageUri;
    private DatabaseReference usersRef;
    // grab the widgets as objects
    private EditText etName;
    private EditText etLocation;
    private EditText etEmail;
    private OnFragmentInteractionListener mListener;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    public static EditProfileFragment newInstance() {
        EditProfileFragment fragment = new EditProfileFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("Users").child(currentUser.getUid().toString());

        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        rootView.findViewById(R.id.buttonEditProfileEditProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfo(null);
                getFragmentManager().popBackStack();
            }
        });
        rootView.findViewById(R.id.buttonEditProfileBackToProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();
            }
        });;
        etName = (EditText) rootView.findViewById(R.id.etEditProfileName);
        etLocation = (EditText) rootView.findViewById(R.id.etEditProfileLocation);
        etEmail = (EditText)  rootView.findViewById(R.id.etEditProfileEmail);
        ivProfileImage = (ImageView) rootView.findViewById(R.id.ivEditProfile);
        etEmail.setText(currentUser.getEmail());

        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
                photoGalleryIntent.setType("image/*");
                //photoGalleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(photoGalleryIntent, PICK_PHOTO_FROM_GALLERY);
            }
        });
        //This finds the photo data by the job id from firebase storage, nothing is passed around
        StorageReference jobPhotoRef = storage.getReference().child("userprofilephotos").child(currentUser.getUid());
        jobPhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                // Pass it to Picasso to download, show in ImageView and caching
                Picasso.with(getContext()).load(uri.toString()).into(ivProfileImage);
            }
        });
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                etName.setText(dataSnapshot.child("name").getValue().toString());
                etLocation.setText(dataSnapshot.child("location").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return rootView;
    }
    public void updateUserInfo(View v) {
        String newName = etName.getText().toString();
        String newLoc = etLocation.getText().toString();
        String newEmail = etEmail.getText().toString();

        // Set name of user and location
        if (!newName.isEmpty() && !newLoc.isEmpty()) {
            usersRef.setValue(new User(newName, newLoc));
        }
        if (ivProfileImage.getDrawable() != null) {
            StorageReference pathReference = storage.getReference().child("userprofilephotos").child(currentUser.getUid());
            pathReference.putFile(imageUri);
        }
        if (!newEmail.equals("") || newEmail.contains("@") || !newEmail.equals(currentUser.getEmail())){
            currentUser.updateEmail(newEmail);
        }
        startActivity(new Intent(getContext(), ViewProfileActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri targetURI = data.getData();
            ivProfileImage.setImageURI(targetURI);
            imageUri = targetURI;
        }
    }
    @Override
    public void  onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.profileMenuSettings).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
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
