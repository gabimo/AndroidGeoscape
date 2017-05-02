package com.lawnscape;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PostJobActivity extends AppCompatActivity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private FirebaseDatabase database;
    //photo vars
    private ImageView ivPostJobPhoto;
    private final int PICK_PHOTO_FROM_GALLERY = 5;
    //Location Vars
    private Location myCurLoc;
    private final int PERMISSION_ACCESS_COARSE_LOCATION = 1;// no reason, just a 16 bit number
    private boolean LOCATION_SERVICES_ENABLED;

    private EditText etTitle;
    private EditText etLocation;
    private EditText etDescription;
    private GridView gvUploadPhotos;
    private ArrayList<Uri> localUriList;
    private PhotoAdapter photoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check for access to location data
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If no access to location services, then ask for permission
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }else{
            LOCATION_SERVICES_ENABLED = true;
        }
        setContentView(R.layout.activity_post_job);
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        ivPostJobPhoto = (ImageView) findViewById(R.id.ivPostJobButton);
        gvUploadPhotos = (GridView) findViewById(R.id.gvPhotoUploads);
        localUriList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(this, localUriList);
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
        //make sure user is logged in and has an account
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(PostJobActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
/**************** END LIFECYCLE METHODS ******************/
    public void postJob(View v) {
        v.setEnabled(false);
        if (LOCATION_SERVICES_ENABLED) {
            // Acquire a reference to the system Location Manager
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
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
                etTitle = (EditText) findViewById(R.id.etPostJobTitle);
                etLocation = (EditText) findViewById(R.id.etPostJobLocation);
                etDescription = (EditText) findViewById(R.id.etPostJobDescription);
                String newTitle = etTitle.getText().toString();
                String newLoc = etLocation.getText().toString();
                String newDesc = etDescription.getText().toString();
                String userID = currentUser.getUid().toString();
                String lat = String.valueOf(myCurLoc.getLatitude());
                String lng = String.valueOf(myCurLoc.getLongitude());
                //Add the job to a list of jobs for the user
                DatabaseReference myJobsRef = database.getReference("Jobs");
                DatabaseReference myUserJobRef = database.getReference("Users").child(currentUser.getUid().toString()).child("jobs").push();
                // Add a job, newJobRef will now hold the jobid value(a string)
                DatabaseReference newJobRef = myJobsRef.push();
                if (newDesc.equals("")) {
                    newDesc = "No description";
                }
                //Records current date and time
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd mm:ss");
                Job newJob = new Job(sdf.format(new Date()), newTitle, newLoc, newDesc, userID, newJobRef.getKey(), lat, lng);
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
                finish();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }
        v.setEnabled(true);
    }
/******************************* NECESSARY FOR ************************************/
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Your location will be used to display your post on a map.")
                            .setTitle("Please enable location settings")
                            .create()
                            .show();
                }
                break;
        }
    }
    // This is nothing more than a variable stored for later user
    LocationListener locationListener = new LocationListener() {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_FROM_GALLERY && resultCode == RESULT_OK) {
            Uri targetURI = data.getData();
            localUriList.add(targetURI);
            photoAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra("View", "all");
                if (upIntent != null && NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder builder = TaskStackBuilder.create(this);
                    builder.addNextIntentWithParentStack(upIntent);
                    builder.startActivities();
                } else {
                    if (upIntent != null) {
                        upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        this.startActivity(upIntent);
                        this.finish();
                    } else {
                        upIntent = new Intent( this, ViewJobsListsActivity.class);
                        upIntent.putExtra("View", "all");
                        startActivity(upIntent);
                    }
                }
                return true;
        }
        return false;
    }
}
