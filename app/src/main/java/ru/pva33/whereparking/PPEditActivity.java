package ru.pva33.whereparking;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import ru.pva33.whereparking.db.ParkingPoint;


//public class PPEditActivity extends ActionBarActivity implements View.OnClickListener {
public class PPEditActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "PVA_DEBUG";
    private ParkingPoint pp;

    private EditText edName, edLongitude, edLatitude;
    private TextView tvSoundPath;
    private Button bSave;
    private ImageButton bSound, bMap;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppedit);
        Log.e(TAG, "PPEditActivity.onCreate Boundle=" + savedInstanceState);
        edName = (EditText) findViewById(R.id.edName);
        edLongitude = (EditText) findViewById(R.id.edLongitude);
        edLatitude = (EditText) findViewById(R.id.edLatitude);
        tvSoundPath = (TextView) findViewById(R.id.edSoundPath);
        bSound = (ImageButton) findViewById(R.id.bSound);
        bMap = (ImageButton) findViewById(R.id.bMap);
        bSave = (Button) findViewById(R.id.bSave);
        if (savedInstanceState == null) {
            this.pp = (ParkingPoint) getIntent().getExtras().get("parkingPoint");
        } else {
            this.pp = (ParkingPoint) savedInstanceState.getSerializable("parkingPoint");
        }
        showData(this.pp);
    }

    /**
     * Serialize current {@link ParkingPoint} and store data. Usefull on orientation change.
     * Stored data would used later in {@link #onCreate(Bundle)}.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("parkingPoint", pp);
    }


    private void showData(ParkingPoint pp) {
        edName.setText(pp.getName());
        tvSoundPath.setText(pp.getSoundPath());
        edLongitude.setText(String.valueOf(pp.getLongitude()));
        edLatitude.setText(String.valueOf(pp.getLatitude()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ppedit, menu);
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

    @Override
    public void onClick(View v) {
        // return modifyed parking point
        //refresh data in parking point
        if (v == bSave) {
            if (pp != null) {
                pp.setName(edName.getText().toString());
                pp.setSoundPath(tvSoundPath.getText().toString());
                pp.setLongitude(new Double(edLongitude.getText().toString()));
                pp.setLatitude(new Double(edLatitude.getText().toString()));
            }
            Intent intent = new Intent();
            intent.putExtra("parkingPoint", this.pp);
            setResult(RESULT_OK, intent);
            finish();
        } else if (v == bSound) {
            Intent intent = new Intent(this, SoundRecorderActivity.class);
            Log.e(TAG, "Sound button click. pp.getSoundPath()='" + pp.getSoundPath() + "'");
            String fileName = pp.getSoundPath();
            if (fileName == null || fileName.isEmpty()) {
                fileName = ParkingHelper.getSoundFileName(this, pp);
            }
            Log.e(TAG, "fileName from after helper =" + fileName);
            intent.putExtra("fileName", fileName);
            intent.putExtra("soundKeeper", pp);
            startActivityForResult(intent, ParkingHelper.EDIT_SOUND);

        } else if (v == bMap) {
            Intent intent = new Intent(this, MapActivity.class);
            ArrayList<ParkingPoint> ppList = new ArrayList<>();
            ppList.add(pp);
            intent.putExtra(MapActivity.DATA_KEY, ppList);
            startActivityForResult(intent, ParkingHelper.SHOW_MAP);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult fired. result=" + resultCode + " data=" + data);
        switch (requestCode) {
            case ParkingHelper.EDIT_SOUND:
                Log.d(TAG, "ppedit onactivity result fired. data=" + data);
                String fileName = null;
                if (data != null) {
                    fileName = data.getStringExtra("fileName");
                    Log.d(TAG, "Get fileName from recorder = <" + fileName + ">");
                }
                this.pp.setSoundPath(fileName);
            case ParkingHelper.SHOW_MAP:
                if (data != null) {
                    ArrayList list = (ArrayList) data.getExtras().get(MapActivity.DATA_KEY);
                    // we expect single row in list
                    if (list != null && list.size() > 0) {
                        ParkingPoint _pp = (ParkingPoint) list.get(0);
                        this.pp.setPosition(_pp.getPosition());
                    }
                }
        }
        showData(pp);
    }

    @Override
    public void onBackPressed() {
        // when user press 'back' hardware button
//        super.onBackPressed();
        onClick(bSave);
    }
}
