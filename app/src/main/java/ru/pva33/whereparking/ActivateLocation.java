package ru.pva33.whereparking;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by pva on 20.01.16.
 */
public class ActivateLocation implements LocationListener {

    public static final int GPS = 0;
    /**
     * LocationManager.GPS_PROVIDER*
     */
    public static final int NETWORK = 1;
    /**
     * LocationManager.NETWORK_PROVIDER*
     */
    public static final int PASSIVE = 2;
    /**
     * LocationManager.PASSIVE_PROVIDER*
     */
    public static final int DEBUG = 7;
    private Location location;
    private LocationManager locationManager;

    /**
     * return test data*
     */

    public ActivateLocation(int PROVIDER, Context context) {
        Log.i("PVA_DEBUG", context.toString());
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        switch (PROVIDER) {
            case GPS: {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                break;
            }
            case NETWORK: {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                                                       this);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                break;
            }
            case PASSIVE: {
                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0,
                                                       this);
                location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                break;
            }
            case DEBUG: {
                location = new Location("Debug");
                location.reset();
                location.setLatitude(53.204509); // Широта
                location.setLongitude(50.16056);// Долгота

            }
        }
    }

    public void unregisterLocationListener() {
        locationManager.removeUpdates(this);
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
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
}
