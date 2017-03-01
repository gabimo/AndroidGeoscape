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
    private final int PERMISSION_ACCESS_COARSE_LOCATION = 1;// no reason, just a 16 bit number
    private boolean LOCATION_SERVICES_ENABLED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_job);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            LOCATION_SERVICES_ENABLED = false;
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_COARSE_LOCATION);
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
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 0, (float) 0, locationListener);
                String provider = locationManager.getBestProvider(new Criteria(), true);
                myCurLoc = locationManager.getLastKnownLocation(provider);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            if (myCurLoc != null) {
                String lat = String.valueOf(myCurLoc.getLatitude());
                String lng = String.valueOf(myCurLoc.getLongitude());

                TextView etTitle = (TextView) findViewById(R.id.etPostJobTitle);
                TextView etLocation = (TextView) findViewById(R.id.etPostJobLocation);
                TextView etDescription = (TextView) findViewById(R.id.etPostJobDescription);

                String newTitle = etTitle.getText().toString();
                String newLoc = etLocation.getText().toString();
                String newDesc = etDescription.getText().toString();
                String userID = currentUser.getUid().toString();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myJobsRef = database.getReference("Jobs");
                DatabaseReference myUserJobRef = database.getReference("Users").child(currentUser.getUid().toString()).child("jobs").push();

                // Add a job
                DatabaseReference newJobRef = myJobsRef.push();
                if (newDesc.equals("")) {
                    newDesc = "No description";
                }
                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd mm:ss");
                Job newJob = new Job(sdf.format(new Date()), newTitle, newLoc, newDesc, userID, newJobRef.getKey(), lat, lng);
                newJobRef.setValue(newJob);
                myUserJobRef.setValue(newJobRef.getKey());
                Intent showLocOnMap = new Intent(this, MapJobsActivity.class);
                showLocOnMap.putExtra("latitude", lat);
                showLocOnMap.putExtra("longitude", lng);
                startActivity(showLocOnMap);
                finish();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }
/**********************************************************************************/
/************************** LOCATION GETTING STUFF ********************************/
    @Override
    public void onRequestPermissionsResult ( int requestCode, String[] permissions,
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

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            myCurLoc = location;
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) { }
    };

}
