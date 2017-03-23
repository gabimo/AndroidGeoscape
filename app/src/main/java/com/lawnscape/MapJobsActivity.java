package com.lawnscape;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapJobsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private ArrayList<Job> jobsList;
    private ArrayList<String> jobsToFetch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_jobs);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //start anim
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        // Add a marker in Sydney and move the camera;
        DatabaseReference jobsRef = FirebaseDatabase.getInstance().getReference("Jobs");

        jobsToFetch = getIntent().getStringArrayListExtra("JobsList");
        if(jobsToFetch != null){
            for(String jobid: jobsToFetch){
                jobsRef.child(jobid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String title = (String) dataSnapshot.child("title").getValue();
                        String location = (String) dataSnapshot.child("location").getValue();
                        String description = (String) dataSnapshot.child("description").getValue();
                        String date = (String) dataSnapshot.child("date").getValue();
                        String lat = (String) dataSnapshot.child("latitude").getValue();
                        String lng = (String) dataSnapshot.child("longitude").getValue();
                        String userid = (String) dataSnapshot.child("userid").getValue();
                        String postid = dataSnapshot.getKey().toString();
                        Job newJob = new Job(date, title, location, description, userid, postid, lat, lng);
                        //jobsList.add(newJob);
                        LatLng loc = new LatLng(Double.valueOf(lat), Double.valueOf(lng));
                        // add a point on the map
                        mMap.addMarker(new MarkerOptions().position(loc).title(newJob.getTitle()));
                        //zoom and focus
                        mMap.animateCamera(CameraUpdateFactory.zoomIn());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        }else {
            jobsList = new ArrayList<Job>();
            jobsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot jobNode : dataSnapshot.getChildren()) {
                        String title = (String) jobNode.child("title").getValue();
                        String location = (String) jobNode.child("location").getValue();
                        String description = (String) jobNode.child("description").getValue();
                        String date = (String) jobNode.child("date").getValue();
                        String lat = (String) jobNode.child("latitude").getValue();
                        String lng = (String) jobNode.child("longitude").getValue();
                        String userid = (String) jobNode.child("userid").getValue();
                        String postid = (String) jobNode.getKey().toString();
                        Job newJob = new Job(date, title, location, description, userid, postid, lat, lng);
                        jobsList.add(newJob);
                        LatLng loc = new LatLng(Double.valueOf(lat), Double.valueOf(lng));
                        // add a point on the map
                        Marker m = mMap.addMarker(new MarkerOptions().position(loc).title(newJob.getTitle()));

                        //zoom
                        mMap.animateCamera(CameraUpdateFactory.zoomIn());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }



}
