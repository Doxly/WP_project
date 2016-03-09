package ru.pva33.whereparking;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.MediaPlayer;
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
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.pva33.whereparking.db.DatabaseHelper;
import ru.pva33.whereparking.db.ParkingPoint;

/**
 * Class work as background service.
 * It periodically catch current geolocation to perform check wheither we approach to
 * one of our parking point. when distance to parking poing becames small, service choose
 * parking side from these parking point and play sound for that side.
 * Service create notification with active item to stop service.
 */
public class PvaParkingService extends Service
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
    LocationListener {
    // IntentService handle all intent in que and terminate itself.

    public static final String CLOSE_ACTION = "close";
    private static final String TAG = "PVA_DEBUG";
    private static final long TIMER_DELAY = 1000L;
    private static final long TIMER_PERIOD = 60 * 1000L;
    //    Some code for notification icon
    private static final int NOTIFICATION = 1;
    private final NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(
        this
    );
    private Timer timer;
    private GoogleApiClient googlcApiClient;
    private Location location;
    //    private String locationMethod;
    // distance when closer - fire side choosing
    private int fireDistance;
    // Cach of ParkingPoints with there coordinates
    private List<ParkingPoint> parkingPoints;
    //    private String soundFileName;
    private DatabaseHelper databaseHelper;
    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            Log.d(TAG, "Timer task doing work");
            location = LocationServices.FusedLocationApi.getLastLocation(googlcApiClient);

            if (location != null) {
                Log.d(TAG, location.toString());
                play();
            }
        }
    };
    private NotificationManager mNotificationManager = null;

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
//        locationMethod = prefs.getString("location_method", "7");
//        int locationProvider = Integer.valueOf(locationMethod);
        fireDistance = Integer.valueOf(prefs.getString("fire_distance", "2"));
//        Log.d(TAG, "location method from prefs=" + locationProvider);
        parkingPoints = new ArrayList<>();

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

    private void play() {
        MediaPlayer mp = new MediaPlayer();
//        try {
//            mp.setDataSource(soundFileName);
//            mp.prepare();
//            mp.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Fired when connected to google location service
     *
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        this.location = LocationServices.FusedLocationApi.getLastLocation(googlcApiClient);
        // in order to get location periodically
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(TIMER_PERIOD);
        locationRequest.setFastestInterval(TIMER_PERIOD);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        Collection<LocationRequest> requests = new ArrayList<>();
        requests.add(locationRequest);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addAllLocationRequests(requests);

        PendingResult<LocationSettingsResult> result =
            LocationServices.SettingsApi.checkLocationSettings(googlcApiClient, builder.build());


        startLocationUpdates(locationRequest);
    }

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

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        Log.d(TAG, "onLocationChanged fired. new location=" + location);
    }
}
