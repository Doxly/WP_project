package ru.pva33.whereparking;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.pva33.whereparking.db.DatabaseHelper;
import ru.pva33.whereparking.db.ParkingPoint;

public class PvaParkingService extends Service {
    // IntentService handle all intent in que and terminate itself.
//public class PvaParkingService extends IntentService {

    public static final String CLOSE_ACTION = "close";
    private static final String TAG = "PVA_DEBUG";
    //    Some code for notification icon
    private static final int NOTIFICATION = 1;
    private final NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(
        this
    );
    private Timer timer;
    private ActivateLocation aloc;

    private Location location;
    private String locationMethod;
    // distance when closer - fire side choosing
    private int fireDistance;
    // Cach of ParkingPoints with there coordinates
    private List<ParkingPoint> parkingPoints;
    private String soundFileName;
    private DatabaseHelper databaseHelper;
    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            Log.d(TAG, "Timer task doing work");
            if (aloc != null) {
                location = aloc.getLocation();
            }

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        location = lm.getLastKnownLocation("0");
//        if (location != null)
//            Log.d(TAG, location.toString());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        locationMethod = prefs.getString("location_method", "7");
        int locationProvider = Integer.valueOf(locationMethod);
        fireDistance = Integer.valueOf(prefs.getString("fire_distance", "2"));
        Log.d(TAG, "location method from prefs=" + locationProvider);
        parkingPoints = new ArrayList<>();
        // fill parking points
//        Log.d(TAG, "location method from prefs="+locationMethod);
//        if (locationMethod.equals("1")){
//            locationProvider = ActivateLocation.GPS;
//        }else if (locationMethod.equals("2")){
//            locationProvider = ActivateLocation.NETWORK;
//        }
        aloc = new ActivateLocation(locationProvider, this);
//        aloc = new ActivateLocation(ActivateLocation.GPS, this);
        location = aloc.getLocation();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        soundFileName = intent.getStringExtra("fileName");
        Log.d(TAG, "onStart:name from intent=" + soundFileName);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        soundFileName = intent.getStringExtra("fileName");
        Log.d(TAG, "onBind:name from intent=" + soundFileName);
        return null;
    }

    //    @Override
    protected void onHandleIntent(Intent intent) {
        //Here we can get some data passed from outside
        soundFileName = intent.getStringExtra("fileName");
        Log.d(TAG, "onHandleIntent: " + soundFileName);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service creating");

        timer = new Timer("PvaWhereParkingTimer");
        timer.schedule(updateTask, 1000L, 60 * 1000L);
//        for notification icon
        setupNotifications();
        showNotification();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroying");

        timer.cancel();
        timer = null;

        aloc.unregisterLocationListener();
        aloc = null;
        if (mNotificationManager != null) {
            mNotificationManager.cancel(NOTIFICATION);
        }
    }

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

}
