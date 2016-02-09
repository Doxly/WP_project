package ru.pva33.whereparking;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;

import java.io.IOException;


public class SoundRecorderActivity extends AppCompatActivity {//ActionBarActivity {

    private static final String TAG = "PVA_DEBUG";

    ImageButton recordButton = null;
    Chronometer chronometer = null;
    boolean mIsRecording = false;

    MediaRecorder mRecorder = null;
    private String mFileName;

    View.OnClickListener clicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIsRecording = !mIsRecording;
            onRecord(mIsRecording);
            showRecordStatus(mIsRecording);
        }
    };

    /**
     * Save record state before activity would destroyed
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isStartRecording", this.mIsRecording);
        Log.d(TAG, "SaveInstanceState done." + mIsRecording);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.mIsRecording = savedInstanceState.getBoolean("isStartRecording");
            Log.d(TAG, "RestoreInstanceState done." + mIsRecording);
        }
        showRecordStatus(this.mIsRecording);
    }

    private void showRecordStatus(boolean isRecording) {
        if (isRecording) {
            recordButton.setBackgroundResource(R.drawable.mic1);
        } else {
            recordButton.setBackgroundResource(R.drawable.mic);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_recorder);
        recordButton = (ImageButton) findViewById(R.id.recofdButton);
        recordButton.setOnClickListener(clicker);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
//        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
//        mFileName += "/pvatest.3gp";

//        mFileName = savedInstanceState.getString("fileName");
        mFileName = getIntent().getStringExtra("fileName");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sound_recorder, menu);
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

    private void onRecord(boolean isRecording) {
        if (isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        chronometer.stop();
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        mRecorder.start();
        chronometer.start();
    }
}
