package com.lawnscape;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Mellis on 2/27/2017.
 */

public class GetCurrentLocationListener implements LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {
    final static int REQUEST_COARSE_LOCATION_ACCESS = 91;
    private LocationManager locationManager;

    public GetCurrentLocationListener(Activity cntxt){
        if (ContextCompat.checkSelfPermission(cntxt, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(cntxt,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION_ACCESS);
        }else {
            locationManager = (LocationManager) cntxt.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long) 0, (float) 0, this);
        }

    }
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    System.out.println("FAILED TO GET PERMISSION");

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


}
