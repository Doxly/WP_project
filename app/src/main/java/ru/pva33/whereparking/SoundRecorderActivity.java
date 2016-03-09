package ru.pva33.whereparking;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import ru.pva33.whereparking.db.SoundKeeper;


//public class SoundRecorderActivity extends AppCompatActivity implements View.OnClickListener {//ActionBarActivity {
public class SoundRecorderActivity extends Activity implements View.OnClickListener {//ActionBarActivity {

    private static final String TAG = "PVA_DEBUG";
    /* Our sound recorder may be in one of this states: idle, recording, playing
    * */

    private static final int IDLE = 0,
        RECORDING = 1,
        PLAYING = 2;
    ImageButton bRecord, bPlay, bDelete;
    Chronometer chronometer;
    TextView tvFileName;
    SoundKeeper soundKeeper;
    MediaRecorder mRecorder = null;
    MediaPlayer mPlayer = null;
    private int state = IDLE;
    private String mFileName;


    /**
     * Save record_enable state before activity would destroyed
     *
     * @param outState bundle to save some data
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("state", state);
//        outState.putBoolean("isStartRecording", this.mIsRecording);
//        Log.d(TAG, "SaveInstanceState done." + mIsRecording);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.state = savedInstanceState.getInt("state", IDLE);
//            this.mIsRecording = savedInstanceState.getBoolean("isStartRecording");
//            Log.d(TAG, "RestoreInstanceState done." + mIsRecording);
        }
        showState(state);
    }

    private void showState(int state) {
        switch (state) {
            case RECORDING:
                bRecord.setBackgroundResource(R.drawable.record);
                bPlay.setBackgroundResource(R.drawable.play_disable);
                break;
            case PLAYING:
                bRecord.setBackgroundResource(R.drawable.record_disable);
                bPlay.setBackgroundResource(R.drawable.play);
                break;
            case IDLE:
                bRecord.setBackgroundResource(R.drawable.record_enable);
                if (ParkingHelper.fileExists(mFileName)) {
                    bPlay.setBackgroundResource(R.drawable.play_enable);
                } else {
                    bPlay.setBackgroundResource(R.drawable.play_disable);
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_recorder);
        bRecord = (ImageButton) findViewById(R.id.ibRecord);
        bRecord.setOnClickListener(this);
        bPlay = (ImageButton) findViewById(R.id.ibPlay);
        bPlay.setOnClickListener(this);
        bDelete = (ImageButton) findViewById(R.id.ibDelete);
        bDelete.setOnClickListener(this);
        chronometer = (Chronometer) findViewById(R.id.chronometer);
        tvFileName = (TextView) findViewById(R.id.tvFileName);
//        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
//        mFileName += "/pvatest.3gp";

//        mFileName = savedInstanceState.getString("fileName");
        mFileName = getIntent().getStringExtra("fileName");
        soundKeeper = (SoundKeeper) getIntent().getExtras().get("soundKeeper");
//        soundKeeperDao = (Dao) getIntent().getExtras().get("soundKeeperDao");
        tvFileName.setText(mFileName);
        showState(state);
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

    private void onRecord(int state) {
        switch (state) {
            case IDLE:
                startRecording();
                switchState(RECORDING);
                break;
            case RECORDING:
                stopRecording();
                switchState(RECORDING);
                break;
        }
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        chronometer.stop();
        if (soundKeeper != null) {
            soundKeeper.setSoundPath(mFileName);
//            try {
//                soundKeeperDao.update(soundKeeper);
//            } catch (SQLException e) {
//                String text = "error updating DB with soundPath";
//                Log.e(TAG, text);
//                Log.d(TAG, text + e.getMessage());
//                MainActivity.alert(this, text);
//            }
        }
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
        Log.e(TAG, "Start recording pressed. file name=" + mFileName);
        mRecorder.start();
        chronometer.start();
    }

    @Override
    public void onBackPressed() {
        // when hardware back button pressed - store result
//        super.onBackPressed();
        Intent intent = new Intent();
        if (ParkingHelper.fileExists(mFileName)) {
            intent.putExtra("fileName", this.mFileName);
            Log.d(TAG, "write file name=" + mFileName);
        }
        intent.putExtra("soundKeeper", (Serializable) this.soundKeeper);
        setResult(RESULT_OK, intent);
        Log.d(TAG, "Recorder onBack. setResult done. intent=" + intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == bRecord) {
            onRecord(state);
//            switchState(RECORDING);
//            mIsRecording = !mIsRecording;
        } else if (v == bPlay) {
            onPlay(state);
            switchState(PLAYING);
        } else if (v == bDelete) {
            removeFile(mFileName);
        }
        showState(state);
    }

    private void removeFile(String mFileName) {
        File f = new File(mFileName);
        f.delete();// if file doesn't exists no exception fired
        soundKeeper.setSoundPath(null);
    }

    private void onPlay(int state) {
        switch (state) {
            case IDLE:
                startPlay();
                switchState(PLAYING);
                break;
            case PLAYING:
                stopPlay();
//                switchState(PLAYING);
        }
    }

    private void stopPlay() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        state = IDLE;
    }

    private void startPlay() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        switchState(PLAYING);
                        showState(state);
                    }
                }
            );
            switchState(PLAYING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchState(int targetState) {
        state = state == targetState ? IDLE : targetState;
    }



}
