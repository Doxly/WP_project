package ru.pva33.whereparking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


//public class ParkingPointListActivity extends ActionBarActivity {
public class ParkingPointListActivity extends AppCompatActivity {

    private static final String TAG = "PVA_DEBUG";
    DatabaseHelper databaseHelper;
    Dao<ParkingPoint, Integer> parkingPointDao;
    AdapterView.OnItemClickListener clicker = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "clicker work");
            // call ppdetail activity
            ParkingPoint pp = (ParkingPoint) parent.getAdapter().getItem(position);
            Intent intent = new Intent(ParkingPointListActivity.this, PPDetailActivity.class);
            intent.putExtra("parkingPoint", pp);
            intent.putExtra("parkingPointId", pp.get_id());
            startActivity(intent);
        }
    };
    AdapterView.OnItemLongClickListener longClicker = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Log.d(TAG, "long clicker work");
            return false;
        }
    };
    private int selectedRowPosition = -1;
    private ListView listView;
    private List<ParkingPoint> parkingPointList;
    private ParkingPoint currentParkingPoint;

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
        setContentView(R.layout.activity_parking_point_list);
        listView = (ListView) findViewById(R.id.listView);
        // get data from db and put them to list
        try {
            parkingPointDao = getDatabaseHelper().getParkingPontDao();
            parkingPointList = parkingPointDao.queryForAll();

            parkingPointList.add(new ParkingPoint("fake point", 0, 0));

            Log.d(TAG, "ppList filled from db" + parkingPointList);
            // inflate list with data
            final LayoutInflater inflater = (LayoutInflater) this.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
            );
            final View rowView = inflater.inflate(R.layout.pp_list_item, listView, false);
            // attach listeners to view. click show detail information. Long click show popup menu with list of operations
            setListeners(listView);
//            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
//
//                                                @Override
//                                                public void onItemClick(AdapterView<?> parent,
//                                                                        View view, int position,
//                                                                        long id) {
//                                                    Log.d(TAG, "new click listener fired!");
//                                                    Toast.makeText(ParkingPointListActivity.this, "litener", Toast.LENGTH_LONG).show();
//                                                }
//                                            });
            // set adapter
            listView.setAdapter(
                new PPArrayAdapter(this, R.layout.pp_list_item, parkingPointList, parkingPointDao)
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setListeners(ListView view) {
        view.setOnItemClickListener(clicker);
        view.setOnItemLongClickListener(longClicker);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_parking_point_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            // TODO add new parking point here
            /*Add to DB
            * put in list
            * make new record current
            * call edit of new record*/
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
