package ru.pva33.whereparking;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ru.pva33.whereparking.db.DatabaseHelper;
import ru.pva33.whereparking.db.ParkingPoint;
import ru.pva33.whereparking.db.ParkingSide;


public class PPDetailActivity extends ActionBarActivity {

    private static final String TAG = "PVA_DEBUG";
    ParkingPoint pp;
    TextView name, soundPath, longitude, latitude;
    DatabaseHelper databaseHelper;
    Dao<ParkingSide, Integer> parkingSideDao;
    AlertDialog recordPanel;
    private boolean dataChanged;
    private ListView listView;
    private PSArrayAdapter adapter;
    private List<ParkingSide> parkingSideList;
    private ParkingSide currentParkingSide;

    private AdapterView.OnItemClickListener clicker = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "Must to show PPSide details");
        }
    };

    private AdapterView.OnItemLongClickListener longClicker = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            currentParkingSide = adapter.getItem(position);
            recordPanel.show();
            return true;
        }
    };


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
        name = (TextView) findViewById(R.id.name);
        longitude = (TextView) findViewById(R.id.longitude);
        latitude = (TextView) findViewById(R.id.latitude);
        soundPath = (TextView) findViewById(R.id.soundPath);
        listView = (ListView) findViewById(R.id.PPDetailListViewPPSides);
        setListeners(listView);

        dataChanged = false;
        if (savedInstanceState == null) {
            this.pp = (ParkingPoint) getIntent().getExtras().get("parkingPoint");
        }else{
            this.pp = (ParkingPoint) savedInstanceState.getSerializable("parkingPoint");
        }
//        if (getIntent() != null && getIntent().getExtras() != null
//            && getIntent().getExtras().get("parkingPoint") != null) {
//            this.pp = (ParkingPoint) getIntent().getExtras().get("parkingPoint");
//        }
//        Log.d(TAG, "PPdetail onCreate. pp="+this.pp);
        showData(this.pp);
        recordPanel = makeRecordPanel();
    }


    private AlertDialog makeRecordPanel() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        View v = this.getLayoutInflater().inflate(R.layout.record_panel, null);
        ab.setView(v);
        AlertDialog res = ab.create();
        return res;
    }

    /**
     * Display data from {@link ParkingPoint} on the screan
     *
     * @param pp
     */
    private void showData(ParkingPoint pp) {
        if (pp == null) {
            return;
        }
        name.setText(pp.getName());
        soundPath.setText("<" + pp.getSoundPath() + ">");
        longitude.setText(String.valueOf(pp.getLongitude()));
        latitude.setText(String.valueOf(pp.getLatitude()));

        // icon for sound file
        ImageButton sb = (ImageButton) findViewById(R.id.detailSoundButton);
        int imageId = pp.getSoundPath() == null ? R.drawable.ic_lock_silent_mode : R.drawable.ic_lock_silent_mode_off;
        sb.setBackgroundResource(imageId);

        // list of parking sides
        final LayoutInflater inflater = (LayoutInflater) getSystemService(
            Context.LAYOUT_INFLATER_SERVICE
        );
        final View rowView = inflater.inflate(R.layout.ps_list_item, listView, false);
        parkingSideList = new ArrayList(pp.getSides());
        try {
            adapter = new PSArrayAdapter(this, R.layout.ps_list_item, parkingSideList,
                getParkingSideDao()
            );
            listView.setAdapter(adapter);
        } catch (SQLException e) {
            Log.d(TAG, e.getMessage());
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * set onItemClickListener and OnItemLongClickListener to list view
     *
     * @param view
     */
    private void setListeners(ListView view) {
        view.setOnItemClickListener(clicker);
        view.setOnItemLongClickListener(longClicker);
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

        switch (id) {
            case R.id.action_edit:
                Intent intent = new Intent(this, PPEditActivity.class);
                intent.putExtra("parkingPoint", this.pp);
                startActivityForResult(intent, ParkingHelper.EDIT_PP);
//                startActivity(intent);
                break;
            case R.id.action_add_side:
                addPS();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Process result of {@link PPEditActivity} call.
     * If there is changed {@link ParkingPoint} data, update parking point in DB
     * and refresh visible data on screan.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // It is possible that user press back button and as a result activity don't return any data
        switch (requestCode) {
            case ParkingHelper.EDIT_PP:
//                Log.d(TAG, "result code = " + resultCode);
                // TODO Need to check resultCode
                if (data != null && data.getExtras() != null) {
                    ParkingPoint newPP = (ParkingPoint) data.getExtras().get("parkingPoint");
                    try {
                        getDatabaseHelper().getParkingPontDao().update(newPP);
                        this.pp = newPP;
                        showData(newPP);
                        this.dataChanged = true;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
//                    Log.d(TAG, "OnActivityResult recieve new parking point:" + newPP);
                }
                break;
            case ParkingHelper.EDIT_PS:
                if(resultCode == RESULT_OK && data != null && data.getExtras() != null){
                    ParkingSide newPS = (ParkingSide) data.getExtras().getSerializable(PSEditActivity.DATA_KEY);
                    try {
                        getDatabaseHelper().getParkingSideDao().update(newPS);
                        adapter.update(newPS);
                        this.dataChanged = true;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * Prepare and set result of this activity ane finish
     */
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("dataChanged", this.dataChanged);
        intent.putExtra("parkingPoint", this.pp);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * This is handler for all record_panel buttons.
     * Button or action is identified by item Tag property.
     * Hides record panel.
     * The name of this method is written in record_panel layout file.
     *
     * @param view
     */
    public void recordPanelClick(View view) {
        String action = view.getTag().toString();
        recordPanel.dismiss();
        Log.d(TAG, "recordPanel Click. " + action);
        switch (action) {
            case "add":
                currentParkingSide = addPS();
                editPS(currentParkingSide);
                break;
            case "edit":
                editPS(currentParkingSide);
                break;
            case "show":
                showPS(currentParkingSide);
                break;
            case "delete":
                deletePS(currentParkingSide);
                break;
        }
    }

    /**
     * Start {@link } for result
     *
     * @param parkingSide
     */
    private void editPS(ParkingSide parkingSide) {
        Log.d(TAG, "would edit parking side");
        Intent intent = new Intent(this, PSEditActivity.class);
        intent.putExtra("parkingSide", parkingSide);
        startActivityForResult(intent, ParkingHelper.EDIT_PS);
    }


    /**
     * Start {@link } activity form result
     *
     * @param parkingSide
     */
    private void showPS(ParkingSide parkingSide) {
        Log.d(TAG, "Would show parking side");
    }

    /**
     * Remove {@link ParkingSide} from list and from DB
     *
     * @param parkingSide
     */
    private void deletePS(ParkingSide parkingSide) {
        try {
            getDatabaseHelper().getParkingSideDao().delete(parkingSide);
            adapter.remove(parkingSide);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create new {@link ParkingSide} and add it to list and to DB.
     * Make it current.
     *
     * @return
     */
    private ParkingSide addPS() {
        currentParkingSide = new ParkingSide(pp, "new parking side");
        try {
            currentParkingSide = getDatabaseHelper().getParkingSideDao().createIfNotExists(
                currentParkingSide
            );
            adapter.add(currentParkingSide);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currentParkingSide;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("parkingPoint", this.pp);
    }
}
