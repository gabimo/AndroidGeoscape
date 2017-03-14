package com.lawnscape;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Provider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PostJobActivity extends Activity {
    //Firebase global init
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Location myCurLoc;

    private final int PERMISSION_ACCESS_FINE_LOCATION = 1;// no reason, just a 16 bit number
    private boolean LOCATION_SERVICES_ENABLED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);
        //Check for access to location data
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // If no access to location services, then ask for permission
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_FINE_LOCATION);
        }


        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        //make sure user is logged in and has an account
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(PostJobActivity.this, LoginActivity.class));
                    finish();
                } else {
                    //user is logged in
                    currentUser = FirebaseAuth.getInstance().getCurrentUser();
                }
            }
        };
    }
    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
/**************** END LIFECYCLE METHODS ******************/
    public void postJob(View v) {
        if (LOCATION_SERVICES_ENABLED) {
            // Acquire a reference to the system Location Manager
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            // Register the listener with the Location Manager to receive location updates
            try {
                //Find the best method to get location(gps, wifi... whatever)
                String provider = locationManager.getBestProvider(new Criteria(), true);
                //This makes the location sensors turn on I think
                locationManager.requestLocationUpdates(provider, (long) 0, (float) 0, locationListener);
                //Set the curLoc, our listener also does this but who cares
                myCurLoc = locationManager.getLastKnownLocation(provider);
                //clean up
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            if (myCurLoc != null) {
                String lat = String.valueOf(myCurLoc.getLatitude());
                String lng = String.valueOf(myCurLoc.getLongitude());

                EditText etTitle = (EditText) findViewById(R.id.etPostJobTitle);
                EditText etLocation = (EditText) findViewById(R.id.etPostJobLocation);
                EditText etDescription = (EditText) findViewById(R.id.etPostJobDescription);

                String newTitle = etTitle.getText().toString();
                String newLoc = etLocation.getText().toString();
                String newDesc = etDescription.getText().toString();
                String userID = currentUser.getUid().toString();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
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
                Intent showLocOnMap = new Intent(this, MapJobsActivity.class);
                startActivity(showLocOnMap);
                finish();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_FINE_LOCATION);
        }
    }
/******************************* NECESSARY FOR ************************************/
/************************** LOCATION GETTING METHOD *******************************/
    // triggered when the user responds to the request for location data
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
    int[] grantResults){
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
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

}
