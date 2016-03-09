package ru.pva33.whereparking;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;

import ru.pva33.whereparking.db.DatabaseHelper;
import ru.pva33.whereparking.db.ParkingPoint;
import ru.pva33.whereparking.db.ParkingSide;


public class MainActivity extends ActionBarActivity {

    public static final String TAG = "PVA_DEBUG";

    DatabaseHelper databaseHelper;
    private boolean withSound;

    public static void alert(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG);
    }

    public boolean isWithSound() {
        // each time when requered we refresh value from settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        withSound = prefs.getBoolean("with_sound", false);
        Log.d(TAG, "With_sound=" + withSound);
        return withSound;
    }

    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("Location_service=", Context.LOCATION_SERVICE);

//        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        Location location = lm.getLastKnownLocation("0");
//        Toast.makeText(getApplicationContext(), "some text", Toast.LENGTH_LONG).show();

        showSide();

        /*Start service if it is not starting yet*/
        Intent serviceIntent = new Intent(PvaParkingService.class.getName());
//        serviceIntent.putExtra("fileName", this.getSoundFileName(1, 1));
        serviceIntent.putExtra("fileName", ParkingHelper.getSoundFileName(this, 1L, 1L));
        startService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
    /*    if (id == R.id.action_settings) {
            showSettings();
//            return true;
        } else if (id == R.id.action_choose) {
            showSide();
        } else if (id == R.id.action_record_sound) {
            showRecorder();
        }*/

        switch (id) {
            case R.id.action_settings:
                showSettings();
                break;
            case R.id.action_choose:
                showSide();
                break;
            case R.id.action_record_sound:
                showRecorder();
                break;
            case R.id.action_exit:
                exit();
                break;
            case R.id.action_pp_list:
                showParkingPoints();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void click(View view) {
        Log.d(TAG, "Test button handler. ");
        // we have some trubles with import android library classes.
        // I try to set it in libs folder
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

//    public String getSoundFileName(int parkingPointId, int parkingSideId) {
//        String path = getExternalFilesDir(null).getAbsolutePath();
//        path += "/" + parkingPointId + "_" + parkingSideId + ".3gp";
//        return path;
//    }

    private void showRecorder() {
//        String path = getSoundFileName(1, 0);
        String path = ParkingHelper.getSoundFileName(this, 1L, 0L);
        Intent intent = new Intent(MainActivity.this, SoundRecorderActivity.class);
        intent.putExtra("fileName", path);
        startActivity(intent);
    }

    private void showSide() {
        try {
            ParkingPoint pp = this.getDatabaseHelper().getParkingPontDao().queryForAll().get(0);
            ParkingSide ps = pp.chooseParkingSide(Calendar.getInstance());
            String text = pp.getName() + ":" + ps.getName();
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            TextView sideText = (TextView) findViewById(R.id.textViewParkingSide);
            sideText.setText(text);
            if (isWithSound()) {
                String path = ParkingHelper.getSoundFileName(this, 1L, 1L);
                playSound(path);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void playSound(String mFileName) {
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(mFileName);
            mp.prepare();
            mp.start();

//            mp.release();
//            mp = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showParkingPoints() {
        Intent intent = new Intent(MainActivity.this, PPListActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (action == null)
            return;
        switch (action) {
            case PvaParkingService.CLOSE_ACTION:
                exit();
                break;
        }
    }

    private void exit() {
        Intent intent = new Intent(this, PvaParkingService.class).setFlags(
            Intent.FLAG_ACTIVITY_CLEAR_TOP
        );
        stopService(intent);
        Log.d(TAG, "Servece stoped ...");
        finish();
    }
}
