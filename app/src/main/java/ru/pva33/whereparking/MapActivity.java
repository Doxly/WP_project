package ru.pva33.whereparking;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ru.pva33.whereparking.db.ParkingPoint;

/**
 * Map can be opened in some different modes:
 * - view/edit single parking point. If parking point has not coordinates onClick would set it.
 * - view several parking points with possibility to choose parking point and show it's details.
 * <p/>
 * data extras is always ppList
 */
//public class MapActivity extends ActionBarActivity // ActionBarActivity is depricated
public class MapActivity extends AppCompatActivity
    implements GoogleMap.OnMarkerDragListener,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    OnMapReadyCallback {
    public final static String DATA_KEY = "ppList";
    private final static int MODE_NO_PP = 0;
    private final static int MODE_SINGL_PP = 1;
    private final static int MODE_MANY_PP = 2;
    private static String TAG = "PVA_DEBUG";
    private GoogleApiClient googleApiClient;
    private int mode = MODE_NO_PP;
    private ArrayList<ParkingPoint> ppList = null;
    private Map<Marker, ParkingPoint> markerMap = null;
    // point where map would centered
    private LatLng latLng = null;
    private GoogleMap map;
    private Location location;

    private int MAP_ANIMATION_DURATION = 2000;
    private float MAP_ZOOM = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // getMap is depricated becouse map may be null in some cases.
//        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(this).
            addConnectionCallbacks(this).
            addOnConnectionFailedListener(this).
            addApi(LocationServices.API).
            build();
        googleApiClient.connect();
    }

    /**
     * Handler for implementation OnMapReadyCallback interface.
     *
     * @param map
     */
    public void onMapReady(GoogleMap map) {
        this.map = map;
        processExtra();
        showMarkers(map);
        UiSettings mapUiSettings = map.getUiSettings();
        mapUiSettings.setZoomControlsEnabled(true);
        mapUiSettings.setMyLocationButtonEnabled(true);
//        mapUiSettings.setMapToolbarEnabled(true);

        moveCamera(latLng, MAP_ZOOM, MAP_ANIMATION_DURATION, map);

    }

    private void moveCamera(LatLng ll, float zoom, int duration, GoogleMap map) {
        Log.d(MainActivity.TAG, "1 move camera ll=" + ll);
        if (ll == null) {
            ll = this.latLng;
            if (ll == null) {
                ll = getCurrentLocation();
            }
        }
        Log.e(MainActivity.TAG, "2 move camera ll=" + ll);
        // zoom in the camera to Davao city
        // if no latlng pass there would exception fired need to get current location
        if (ll != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, zoom));
            // animate the zoom process
            map.animateCamera(CameraUpdateFactory.zoomTo(zoom), duration, null);
        }
    }

    private LatLng getCurrentLocation() {
        return latLng;
    }

    private void showMarkers(GoogleMap map) {
        if (mode != MODE_NO_PP) {
            if (markerMap == null) {
                markerMap = new HashMap<>();
            }
            for (int i = this.ppList.size(); i > 0; i--) {
                ParkingPoint pp = this.ppList.get(i - 1);
                latLng = latLng == null ? pp.getPosition() : latLng;
                Marker marker = map.addMarker(setMarker(pp));
                markerMap.put(marker, pp);
                if (mode == MODE_SINGL_PP) {
                    marker.setDraggable(true);
                }
            }
            map.setOnMarkerDragListener(this);
        }
    }

    /**
     * process any data passed outside by intent extras.
     * Waits {@link #DATA_KEY} keyed data
     */
    private void processExtra() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }
        this.ppList = (ArrayList<ParkingPoint>) extras.get(DATA_KEY);
        if (ppList == null) {
            return;
        }
        if (ppList.size() == 1) {
            this.mode = MODE_SINGL_PP;
        } else {
            this.mode = MODE_MANY_PP;
        }
    }

    /**
     * Create new {@link MarkerOptions} for given {@link ParkingPoint} and position it.
     * @param pp Parking point to show with marker
     * @return
     */
    private MarkerOptions setMarker(ParkingPoint pp) {
        MarkerOptions result = null;
        if (pp != null) {
            latLng = pp.getPosition();
            if (latLng.latitude == 0 || latLng.longitude == 0) {
                latLng = getCurrentLocation();
                // TODO If we make marker at current position, we need to set this position in parking point
                pp.setPosition(latLng);
            }
            result = new MarkerOptions().position(latLng).title(pp.getName()).snippet(
                pp.getSidesText()
            );
        }
        return result;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        setPositionFromMarker(marker);
    }

    /**
     * Find {@link ParkingPoint}, linked with marker.
     * Extract position from marker, and set it to parking point.
     * Set entier list of {@link ParkingPoint} as action result.
     * @param marker Map marker, which has been dragged
     */
    private void setPositionFromMarker(Marker marker) {
        // find marker in hashmap
        // get parkingPointId from hashmap
        ParkingPoint pp = markerMap.get(marker);
        pp.setPosition(marker.getPosition());
        Intent intent = new Intent();
        intent.putExtra(DATA_KEY, ppList);
        setResult(RESULT_OK, intent);

//        Log.d(TAG, "marker position="+marker.getPosition());
//        Log.d(TAG, "ppList="+ppList);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected fired. latLng=" + latLng);
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location == null) {
            return;
        }
        Log.d(TAG, "onConnected. new location=" + location);
        if (latLng == null || (latLng.latitude == 0 && latLng.longitude == 0)) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            moveCamera(latLng, MAP_ZOOM, MAP_ANIMATION_DURATION, map);
            // There is marker with zero coordinates and such pp
            moveZeroMarkers(latLng);
        }
    }

    /**
     *
     * @param latLng
     */
    private void moveZeroMarkers(LatLng latLng) {
        Iterator<Marker> markers = markerMap.keySet().iterator();
        while (markers.hasNext()) {
            Marker marker = markers.next();
            if (marker.getPosition().latitude == 0 && marker.getPosition().longitude == 0) {
                marker.setPosition(latLng);
                setPositionFromMarker(marker);
            }
        }
        // show circle for current position
        map.addCircle(new CircleOptions().
                center(latLng).
//                radius(location.getAccuracy()).
                radius(500).
                strokeWidth(2).
                strokeColor(0).
                fillColor(111)
        );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onDestroy() {
        googleApiClient.disconnect();
        super.onDestroy();
    }
}
