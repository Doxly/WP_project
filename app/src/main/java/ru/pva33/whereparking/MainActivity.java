package ru.pva33.whereparking;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.Calendar;

import ru.pva33.whereparking.db.DatabaseHelper;
import ru.pva33.whereparking.db.ParkingPoint;
import ru.pva33.whereparking.db.ParkingSide;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "PVA_DEBUG";

    DatabaseHelper databaseHelper;

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

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation("0");
//        Toast.makeText(getApplicationContext(), "some text", Toast.LENGTH_LONG).show();

        showSide();

        /*Start service if it is not starting yet*/
        startService(new Intent(PvaParkingService.class.getName()));
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
        if (id == R.id.action_settings) {
            showSettings();
//            return true;
        } else if (id == R.id.action_choose) {
            showSide();
        } else if (id == R.id.action_record_sound) {
            showRecorder();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showRecorder() {
        Intent intent = new Intent(MainActivity.this, SoundRecorderActivity.class);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
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
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        stopService(intent);
        Log.d(TAG, "Servece stoped ...");
        finish();
    }
}
