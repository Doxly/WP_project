package ru.pva33.whereparking;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;

import ru.pva33.whereparking.db.DatabaseHelper;
import ru.pva33.whereparking.db.ParkingPoint;
import ru.pva33.whereparking.db.ParkingSide;


public class PPDetailActivity extends ActionBarActivity {

    private static final String TAG = "PVA_DEBUG";
    ParkingPoint pp;
    DatabaseHelper databaseHelper;
    Dao<ParkingSide, Integer> parkingSideDao;

    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    public Dao<ParkingSide, Integer> getParkingSideDao() throws SQLException {
        if (parkingSideDao == null) {
            parkingSideDao = getDatabaseHelper().getParkingSideDao();
        }
        return parkingSideDao;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppdetail);
        this.pp = (ParkingPoint) getIntent().getExtras().get("parkingPoint");
//        Log.d(TAG, "PPdetail onCreate. pp="+this.pp);
        showData(this.pp);
    }

    private void showData(ParkingPoint pp) {
        TextView tv = (TextView) findViewById(R.id.PPDetailName);
        tv.setText(pp.getName());
        tv = (TextView) findViewById(R.id.PPDetailLaltitude);
        tv.setText(String.valueOf(pp.getLatitude()));
        tv = (TextView) findViewById(R.id.PPDetailLongitude);
        tv.setText(String.valueOf(pp.getLongitude()));
        // icon for sound file
        ImageButton sb = (ImageButton) findViewById(R.id.detailSoundButton);
        int imageId = pp.getSoundPath() == null ? R.drawable.ic_lock_silent_mode : R.drawable.ic_lock_silent_mode_off;
        sb.setBackgroundResource(imageId);
        tv = (TextView) findViewById(R.id.soundPath);
        tv.setText(pp.getSoundPath());

        // list of parking sides
        ListView listView = (ListView) findViewById(R.id.PPDetailListViewPPSides);
        final LayoutInflater inflater = (LayoutInflater) getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        );
        final View rowView = inflater.inflate(R.layout.ps_list_item, listView, false);
        ArrayList list = new ArrayList(pp.getSides());
        try {
            listView.setAdapter(
                new PSArrayAdapter(this, R.layout.ps_list_item, list, getParkingSideDao())
            );
        } catch (SQLException e) {
            Log.d(TAG, e.getMessage());
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ppdetail, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
