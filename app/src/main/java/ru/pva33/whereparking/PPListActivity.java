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
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;

import ru.pva33.whereparking.db.DatabaseHelper;
import ru.pva33.whereparking.db.ParkingPoint;


public class PPListActivity extends ActionBarActivity {
//public class PPListActivity extends AppCompatActivity {

    private static final String TAG = "PVA_DEBUG";
    DatabaseHelper databaseHelper;
    Dao<ParkingPoint, Integer> parkingPointDao;
    AdapterView.OnItemClickListener clicker = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "clicker work");
            // call ppdetail activity
            ParkingPoint pp = (ParkingPoint) parent.getAdapter().getItem(position);
            showPP(pp);
        }
    };
    private AlertDialog recordPanel;
    private ListView listView;
    private PPArrayAdapter adapter;
    private List<ParkingPoint> parkingPointList;
    private ParkingPoint currentParkingPoint;
    AdapterView.OnItemLongClickListener longClicker = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//            Log.d(TAG, "long clicker work");
//            currentParkingPoint = (ParkingPoint) parent.getAdapter().getItem(position);
            currentParkingPoint = adapter.getItem(position);
            recordPanel.show();
            return true;
        }
    };

    public DatabaseHelper getDatabaseHelper() {
        // Initialize helper for future use
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pp_list);
        listView = (ListView) findViewById(R.id.listView);

        // get data from db and put them to list
        try {
            parkingPointDao = getDatabaseHelper().getParkingPontDao();
            parkingPointList = parkingPointDao.queryForAll();
//            Log.d(TAG, "ppList filled from db" + parkingPointList);
            // inflate list with data
            final LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
            );
            final View rowView = inflater.inflate(R.layout.pp_list_item, listView, false);
            // attach listeners to view. onClick show detail information. Long onClick show popup menu with list of operations
            setListeners(listView);
            // set adapter
            adapter = new PPArrayAdapter(this, R.layout.pp_list_item, parkingPointList);
            listView.setAdapter(adapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                currentParkingPoint = addPP();
                editPP(currentParkingPoint);
                break;
            case "edit":
                editPP(currentParkingPoint);
                break;
            case "show":
                showPP(currentParkingPoint);
                break;
            case "delete":
                deletePP(currentParkingPoint);
                break;
        }
    }

    /**
     * Remove {@link ParkingPoint} from list and from DB.
     *
     * @param pp
     */
    private void deletePP(ParkingPoint pp) {
        try {
            adapter.remove(pp);
            getDatabaseHelper().getParkingPontDao().delete(pp);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set OnItemClickListener and OnItemLongClickListener for given ListView
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
        getMenuInflater().inflate(R.menu.menu_pp_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add:
                /*Add to DB
                * put in list
                * make new record_enable current
                * TODO call edit of new record_enable*/
                addPP();
                break;
            case R.id.action_delete:
//                ParkingPoint pp = listView.getAdapter().getItem()
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Add new {@link ParkingPoint} to list and to DB
     *
     * @return
     */
    private ParkingPoint addPP() {
        ParkingPoint res = new ParkingPoint("New parking point", 0, 0);
        try {
            res = getDatabaseHelper().getParkingPontDao().createIfNotExists(res);
            adapter.add(res);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * Start {@link PPDetailActivity} activity for result passing
     * current {@link ParkingPoint} as a parameter.
     *
     * @param pp current parking poing
     */
    private void showPP(ParkingPoint pp) {
        Intent intent = new Intent(PPListActivity.this, PPDetailActivity.class);
        intent.putExtra("parkingPoint", pp);
//        intent.putExtra("parkingPointId", pp.get_id());
        startActivityForResult(intent, ParkingHelper.SHOW_PP);
    }

    /**
     * Start {@link PPEditActivity} activity for result passing
     * current {@link ParkingPoint} as a parameter.
     *
     * @param pp
     */
    private void editPP(ParkingPoint pp) {
        Intent intent = new Intent(PPListActivity.this, PPEditActivity.class);
        intent.putExtra("parkingPoint", pp);
//        intent.putExtra("parkingPointId", pp.get_id());
        startActivityForResult(intent, ParkingHelper.EDIT_PP);
    }

    /**
     * Process results for call of {@link PPEditActivity} and {@link PPDetailActivity}.
     * If there is changed {@link ParkingPoint} data in result, parking point updated in
     * list view and in DB.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult fired. result=" + resultCode + " data=" + data);
        switch (requestCode) {
            case ParkingHelper.EDIT_PP:
                if (resultCode == RESULT_OK) {
                    ParkingPoint pp = (ParkingPoint) data.getExtras().get("parkingPoint");
//                    Log.d(TAG, "get new pp=" + pp);
                    try {
                        getDatabaseHelper().getParkingPontDao().update(pp);
                        adapter.update(pp);
//                        Log.d(TAG, "adapter update done");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case ParkingHelper.SHOW_PP:
                if (resultCode == RESULT_OK) {
                    boolean dataChanged = data.getBooleanExtra("dataChanged", false);
                    if (dataChanged) {
                        ParkingPoint pp = (ParkingPoint) data.getExtras().get("parkingPoint");
                        try {
                            getDatabaseHelper().getParkingPontDao().refresh(pp);
                            adapter.update(pp);
                        } catch (SQLException e) {
                            Log.e(TAG, e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }
}
