package ru.pva33.whereparking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import ru.pva33.whereparking.db.ParkingSide;


//public class PSEditActivity extends ActionBarActivity {
public class PSEditActivity extends Activity
    implements View.OnClickListener{

    private static final String TAG = "PVA_DEBUG";
    public static final String DATA_KEY = "parkingSide";

    private ParkingSide ps;

    private EditText edName, edMessage;
    private TextView tvSoundPath, tvPPName;
    private ImageButton bSound;
    private Button bSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psedit);
        edName = (EditText) findViewById(R.id.edName);
        edMessage = (EditText) findViewById(R.id.edMessage);
        tvSoundPath = (TextView) findViewById(R.id.tvSoundPath);
        tvPPName = (TextView) findViewById(R.id.PPName);
        bSound = (ImageButton) findViewById(R.id.bSound);
        bSave = (Button) findViewById(R.id.bSave);

        if (savedInstanceState != null) {
            this.ps = (ParkingSide) savedInstanceState.getSerializable(DATA_KEY);
        } else{
            this.ps = (ParkingSide) getIntent().getExtras().getSerializable(DATA_KEY);
//            Log.e(TAG, String.format("intent extras=%s", getIntent().getExtras(), getIntent().getExtras().get("par")));
        }
        showData(this.ps);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(DATA_KEY, this.ps);
    }

    private void showData(ParkingSide ps) {
        if (ps == null) {
            Log.e(TAG, "PSEditActivity.showData ps is null.");
            return;
        }
        edName.setText(ps.getName());
        edMessage.setText(ps.getAllowText());
        tvSoundPath.setText(ps.getSoundPath());
        tvPPName.setText(ps.getParkingPoint().getName());
    }


    @Override
    public void onClick(View v) {
        if(v == bSound){
            Intent intent = new Intent(this, SoundRecorderActivity.class);
            String fileName = ps.getSoundPath();
            if (fileName == null || fileName.isEmpty()) {
                fileName = ParkingHelper.getSoundFileName(this, ps);
            }
            intent.putExtra("fileName", fileName);
            intent.putExtra("soundKeeper", ps);
            startActivityForResult(intent, ParkingHelper.EDIT_SOUND);

        }else if(v == bSave){
            if (ps != null) {
                ps.setName(edName.getText().toString());
                ps.setSoundPath(tvSoundPath.getText().toString());
                ps.setAllowText(edMessage.getText().toString());
            }
            Intent intent = new Intent();
            intent.putExtra(DATA_KEY, ps);
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        onClick(bSave);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case ParkingHelper.EDIT_SOUND:
                if (resultCode == RESULT_OK){
                    String fileName = null;
                    if (data != null) {
                        fileName = data.getStringExtra("fileName");
                    }
                    ps.setSoundPath(fileName);
                }
                break;
        }
        showData(ps);
    }
}
