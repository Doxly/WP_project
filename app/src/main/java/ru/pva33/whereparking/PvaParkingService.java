package ru.pva33.whereparking;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import ru.pva33.whereparking.db.DatabaseHelper;
import ru.pva33.whereparking.db.ParkingPoint;
import ru.pva33.whereparking.db.ParkingSide;

/**
 * Class work as background service.
 * It periodically catch current geolocation to perform check wheither we approach to
 * one of our parking point. when distance to parking poing becames small, service choose
 * parking side from these parking point and show notification activity.
 * Service create notification with active item to stop service.
 */
public class PvaParkingService extends Service
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    LocationListener {
    // IntentService handle all intent in que and terminate itself.

    public static final String CLOSE_ACTION = "close";
    private static final String TAG = "PVA_DEBUG";
    private static final long TIMER_IF_EXISTS = 10 * 1000L;
    private static final long TIMER_PERIOD = 60 * 1000L;
    //    Some code for notification icon
    private static final int NOTIFICATION = 1;
    private final NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(
        this
    );
    private GoogleApiClient googlcApiClient;
    private Location location;
    // distance when closer - fire side choosing
    private int fireDistance;
    // Cach of ParkingPoints with there coordinates
    private List<ParkingPoint> ppList;
    private DatabaseHelper databaseHelper;

    private NotificationManager mNotificationManager = null;
    private ParkingSide currentPS;

    public PvaParkingService() {
//        super(null);

        // We can't get preferences here couse this is yet null
//        soundPool = new SoundPool(3, AudioManager.STREAM_NOTIFICATION, 0);
    }

    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    /**
     * Service is called via startService.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        int locationProvider = Integer.valueOf(locationMethod);
        fireDistance = Integer.valueOf(prefs.getString("fire_distance", "2"));
        Log.d(TAG, "fireDistance from prefs=" + fireDistance);
//        ppList = new ArrayList<>();

//        soundFileName = intent.getStringExtra("fileName");
//        Log.d(TAG, "onStartCommand soundFileName: " + soundFileName);


        // For location over googleAPIClient
        googlcApiClient = new GoogleApiClient.Builder(this).
            addConnectionCallbacks(this).
            addOnConnectionFailedListener(this).
            addApi(LocationServices.API).
            build();
        googlcApiClient.connect();
        return super.onStartCommand(intent, flags, startId);
    }


    //    @Override
    protected void onHandleIntent(Intent intent) {
        //Here we can get some data passed from outside
//        soundFileName = intent.getStringExtra("fileName");
//        Log.d(TAG, "onHandleIntent: " + soundFileName);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service creating");

//        timer = new Timer("PvaWhereParkingTimer");
//        timer.schedule(updateTask, TIMER_DELAY, TIMER_PERIOD);
//        for notification icon
        setupNotifications();
        showNotification();
        readPP();
    }

    /**
     * Fill inner array of {@link ParkingPoint} to cache them and avoid db access
     */
    private void readPP() {
        if (ppList == null) {
            try {
                ppList = getDatabaseHelper().getParkingPontDao().queryForAll();
            } catch (SQLException e) {
                Log.e(TAG, "readPP SQLException "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroying");

//        timer.cancel();
//        timer = null;

//        aloc.unregisterLocationListener();
//        aloc = null;
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION);
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(googlcApiClient, this);
        googlcApiClient.disconnect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Create notifications for service
     */
    private void setupNotifications() { // called in onCreate()
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) this.getSystemService(
                NOTIFICATION_SERVICE
            );
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class)
                .setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                ),
            0
        );
        PendingIntent pendingCloseIntent = PendingIntent.getActivity(this, 0,
            new Intent(this,
                MainActivity.class
            )
                .setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                .setAction(
                    CLOSE_ACTION
                ),
            0
        );
        mNotificationBuilder
            .setSmallIcon(R.drawable.parking)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(getText(R.string.app_name))
            .setWhen(System.currentTimeMillis())
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.abc_btn_radio_material, getString(R.string.action_exit),
                pendingCloseIntent
            )
            .setOngoing(true);
        Log.d(TAG, "setupNotifications in service done.");

    }

    private void showNotification() {
        mNotificationBuilder
            // text in short-time pop-up message
            .setTicker("Starting tracking service for where parking")
                // text in details of notification
            .setContentText("tracking service");
        if (mNotificationManager != null) {
            mNotificationManager.notify(NOTIFICATION, mNotificationBuilder.build());
            Log.d(TAG, "showNotification in service done.");
        }
    }


    /**
     * Fired when connected to google location service
     *
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected fired.");
//        this.location = LocationServices.FusedLocationApi.getLastLocation(googlcApiClient);
//        attribute location would set in onLocationChanged listener
        Location loc = LocationServices.FusedLocationApi.getLastLocation(googlcApiClient);
        // in order to get location periodically
        LocationRequest locationRequest = new LocationRequest();
        // set interval for location quering by this application itself
        locationRequest.setInterval(TIMER_PERIOD);
        // set interval for location quering if another application already use location service
        locationRequest.setFastestInterval(TIMER_IF_EXISTS);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        Collection<LocationRequest> requests = new ArrayList<>();
        requests.add(locationRequest);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addAllLocationRequests(requests);

        PendingResult<LocationSettingsResult> result =
            LocationServices.SettingsApi.checkLocationSettings(googlcApiClient, builder.build());


        startLocationUpdates(locationRequest);
        // to quick reaction when connected
        onLocationChanged(loc);
    }

    /**
     *
     * @param locationRequest
     */
    private void startLocationUpdates(LocationRequest locationRequest) {
        LocationServices.FusedLocationApi.requestLocationUpdates(googlcApiClient, locationRequest,
            this
        );
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Would be called periodically when location changed.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged fired. new location="+location);
        Log.d(TAG, "                     current location="+this.location);
        if (location != null && !location.equals(this.location)) {

            this.location = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            Log.d(TAG, "onLocationChanged fired. new location=" + location);
            SolutionMaker solutionMaker = new SolutionMaker(this);
            ParkingPoint pp = solutionMaker.findNearestPP(latLng, fireDistance, ppList);
            if (isInParkMode() && (pp == null ||
                !getCurrentPS().getParkingPoint().get_id().equals(pp.get_id()))) {
                resetParkMode();
            }
            if (pp == null) {
                Log.d(TAG, "No PP near your location");
                return;
            }
            ParkingSide ps = solutionMaker.chooseParkingSide(pp);
            if (isInParkMode() && !getCurrentPS().get_id().equals(ps.get_id())){
                resetParkMode();
            }
            if (!isInParkMode()){
                Log.d(TAG, "Entering ParkMode");
                enterParkMode(ps);
            }
        }
    }

    private void enterParkMode(ParkingSide ps) {
        if (ps == null) {
            return;
        }
        // message activity where parking
        // remember current parking side
        setCurrentPS(ps);
        SolutionMaker solutionMaker = new SolutionMaker(this);
        Calendar endTime = solutionMaker.getEndTime(ps.getParkingPoint());
        long period = solutionMaker.getParkingDuration(ps.getParkingPoint());
        setAlarms(endTime);
        showWhereParking(ps, endTime, period);
    }

    /**
     * start activity to show where parking
     * @param ps
     * @param endTime
     * @param period
     */
    private void showWhereParking(ParkingSide ps, Calendar endTime, long period) {
        if (!isInParkMode()) {
            return;
        }
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("parkingSide", ps);
        intent.putExtra("notificationEndTime", endTime);
        intent.putExtra("period", period);
        intent.putExtra("location", location);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }

    /**
     * set from 0 to 3 alarmes through AlarmManager.
     * see http://www.mobilab.ru/androiddev/androidalarmmanagertutorial.html
     * @param endTime
     */
    private void setAlarms(Calendar endTime) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // get three time from settings


    }

    /**
     * Resert park mode remove if is stored selected parking side.
     * Also remove alarms, that probably was set.
     */
    private void resetParkMode() {
        if (isInParkMode()) {
            setCurrentPS(null);
            removeAlarms();
        }
    }

    /**
     * remove alarms which set by this application if they exists.
     */
    private void removeAlarms() {

    }

    /**
     * Check stored parking side. if it is not null - we in parking mode.
     *
     * @return
     */
    private boolean isInParkMode() {
        return (getCurrentPS() != null);
    }

    private ParkingSide getCurrentPS() {
        return currentPS;
    }

    private void setCurrentPS(ParkingSide ps) {
        currentPS = ps;
    }
}
