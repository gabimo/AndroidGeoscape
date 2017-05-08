package com.lawnscape.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lawnscape.R;
import com.lawnscape.activities.ViewJobsListsActivity;
import com.lawnscape.adapters.PhotoAdapter;
import com.lawnscape.classes.Job;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;


public class PostJobFragment extends Fragment {

    //Firebase global init
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    private final int PICK_PHOTO_FROM_GALLERY = 5;
    //Location Vars
    private Location myCurLoc;
    private final int PERMISSION_ACCESS_COARSE_LOCATION = 1;// no reason, just a 16 bit number
    private boolean LOCATION_SERVICES_ENABLED;

    private EditText etTitle;
    private EditText etLocation;
    private EditText etDescription;
    private Spinner spinnerCategory;
    private ArrayList<Uri> localUriList;
    private PhotoAdapter photoAdapter;

    public PostJobFragment() {
        // Required empty public constructor
    }

    public static PostJobFragment newInstance() {
        PostJobFragment fragment = new PostJobFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post_job, container, false);
        //Check for access to location data
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If no access to location services, then ask for permission
            ActivityCompat.requestPermissions(getActivity(), new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }else{
            LOCATION_SERVICES_ENABLED = true;
        }
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        ImageView ivPostJobPhoto = (ImageView) rootView.findViewById(R.id.ivPostJobUploadPhoto);
        GridView gvUploadPhotos = (GridView) rootView.findViewById(R.id.gvPhotoUploads);
        spinnerCategory = (Spinner) rootView.findViewById(R.id.spinnerCategory);
        etTitle = (EditText) rootView.findViewById(R.id.etPostJobTitle);
        etLocation = (EditText) rootView.findViewById(R.id.etPostJobLocation);
        etDescription = (EditText) rootView.findViewById(R.id.etPostJobDescription);
        localUriList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getActivity(), localUriList);
        gvUploadPhotos.setAdapter(photoAdapter);
        ivPostJobPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
                photoGalleryIntent.setType("image/*");
                //photoGalleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(photoGalleryIntent, PICK_PHOTO_FROM_GALLERY);
            }
        });
        rootView.findViewById(R.id.btnPostJob).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postJob(v);
            }
        });
        return rootView;
    }
    public void postJob(View v) {
        v.setEnabled(false);
        if (LOCATION_SERVICES_ENABLED) {
            // Acquire a reference to the system Location Manager
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            // Register the listener with the Location Manager to receive location updates
            try {
                //Find the best method to get location(gps, wifi... whatever)
                String provider = locationManager.getBestProvider(new Criteria(), true);
                //This makes the location sensors turn on I think
                locationManager.requestLocationUpdates(provider, (long) 0, (float) 0, locationListener);
                //Set the curLoc, our listener also does this but I'm not sure how it works yet
                myCurLoc = locationManager.getLastKnownLocation(provider);
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            if (myCurLoc != null) {
                String newTitle = etTitle.getText().toString();
                String newLoc = etLocation.getText().toString();
                String newDesc = etDescription.getText().toString();
                String userID = mAuth.getCurrentUser().getUid();
                String newCategory = spinnerCategory.getSelectedItem().toString();
                String lat = String.valueOf(myCurLoc.getLatitude());
                String lng = String.valueOf(myCurLoc.getLongitude());
                //Add the job to a list of jobs for the user
                DatabaseReference myJobsRef = database.getReference("Jobs");
                DatabaseReference myUserJobRef = database.getReference("Users").child(mAuth.getCurrentUser().getUid()).child("jobs").push();
                // Add a job, newJobRef will now hold the jobid value(a string)
                DatabaseReference newJobRef = myJobsRef.push();
                if (newDesc.equals("")) {
                    newDesc = "No description";
                }
                //Records current date and time
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd mm:ss");
                Job newJob = new Job(sdf.format(new Date()), newTitle, newLoc, newCategory, newDesc, userID, newJobRef.getKey(), lat, lng);
                //Here the job is actually added
                newJobRef.setValue(newJob);
                myUserJobRef.setValue(newJobRef.getKey());

                //upload the photos if there are any
                if(!localUriList.isEmpty()){
                    StorageReference pathReference = storage.getReference().child("jobphotos").child(newJobRef.getKey());
                    DatabaseReference newJobPhotosRef = newJobRef.child("photoids");
                    DatabaseReference tempRef;
                    pathReference.child("mainphoto").putFile(localUriList.get(0));
                    newJobPhotosRef.child("mainphoto").setValue(true);
                    localUriList.remove(0);
                    //these are the extra photos if there are any
                    for(Uri pic: localUriList){
                        tempRef = newJobPhotosRef.push();
                        tempRef.setValue(true);
                        pathReference.child("otherphotos").child(tempRef.getKey()).putFile(pic);
                    }
                }
                startActivity(new Intent(getContext(),ViewJobsListsActivity.class).putExtra("View","myjobs"));
                getActivity().finish();
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }
        v.setEnabled(true);
    }
/***************************** NECESSARY FOR ************************************/
    /************************** LOCATION GETTING METHOD *******************************/
    // triggered when the user responds to the request for location data
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults){
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LOCATION_SERVICES_ENABLED = true;
                } else {
                    LOCATION_SERVICES_ENABLED = false;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Your location will be used to display your post on a map.")
                            .setTitle("Please enable location settings")
                            .create()
                            .show();
                }
                break;
        }
    }
    // This is nothing more than a variable stored for later user
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            myCurLoc = location;
        }
        //These do not matter since there exists a method bestProvider() returns the best provider
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) { }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri targetURI = data.getData();
            localUriList.add(targetURI);
            photoAdapter.notifyDataSetChanged();
        }
    }
}
