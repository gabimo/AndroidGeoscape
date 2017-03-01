package com.lawnscape;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.location.LocationListener;

/**
 * Created by Mellis on 2/28/2017.
 */

public class CurrentLocationListener implements LocationListener {

    private Location myCurrentLocation;

    public CurrentLocationListener(Context activityContext) {
        super();
    }

    @Override
    public void onLocationChanged(Location location) {
        myCurrentLocation = location;
    }

    public Location getMyCurrentLocation(){
        return myCurrentLocation;
    }

    @Override
    public String toString(){
        if(myCurrentLocation != null) {
            return "Lat: " + String.valueOf(myCurrentLocation.getLatitude()) + " Long: " + String.valueOf(myCurrentLocation.getLongitude());
        }else{
            return "NULL REF";
        }
    }

}
