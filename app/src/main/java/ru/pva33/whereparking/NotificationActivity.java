package ru.pva33.whereparking;

import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ru.pva33.whereparking.db.ParkingPoint;
import ru.pva33.whereparking.db.ParkingSide;


//public class NotificationActivity extends ActionBarActivity {
public class NotificationActivity extends AppCompatActivity {
    private static final String TAG = "PVA_DEBUG";
    long period;
    private ParkingSide currentParkingSide;
    private ParkingPoint currentParkingPoint;
    private Calendar endTime;
    private Location currentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {

            Object extrasObject = extras.get("notificationEndTime");
            if (extrasObject instanceof Calendar) {
                endTime = (Calendar) extrasObject;
            }
            extrasObject = extras.get("parkingSide");
            if (extrasObject instanceof ParkingSide) {
                currentParkingSide = (ParkingSide) extrasObject;
            }
            if (currentParkingSide == null) {
                Log.e(TAG, "currentParkingSide is null. Can't create notification!");
                return;
            }
            currentParkingPoint = currentParkingSide.getParkingPoint();
//            period = extras.getLong("peroid", ParkingSide.INFINITY_PERIOD);
            Calendar now = Calendar.getInstance();
//            Log.d(TAG, "Notification onCreate. notificationEndTime="+endTime);
//            Log.d(TAG, "Notification onCreate. endTime.getTime="+endTime.getTime());
            period = endTime.getTimeInMillis() - now.getTimeInMillis();
            Log.d(TAG, "Notification onCreate. period=" + formatPeriod(period));


            extrasObject = extras.get("location");
            if (extrasObject instanceof Location) {
                currentLocation = (Location) extrasObject;
            }

            TextView tt = (TextView) findViewById(R.id.ttt);
            tt.setText(makeNotificationText());

            playSound();
        }
    }

    /**
     * Play sound for selected {@link ParkingSide} and its {@link ParkingPoint} if they assigned.
     */
    private void playSound() {
        String ppSoundFileName = currentParkingPoint.getSoundPath();
        final String psSoundFileName = currentParkingSide.getSoundPath();
        MediaPlayer mPlayer = new MediaPlayer();
        try {
            if (ppSoundFileName != null && !ppSoundFileName.isEmpty()) {
                mPlayer.setDataSource(ppSoundFileName);
                mPlayer.prepare();
                mPlayer.start();
                mPlayer.setOnCompletionListener(
                    new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            try {
                                if (psSoundFileName != null && psSoundFileName.isEmpty()) {

                                    mp.setDataSource(psSoundFileName);
                                    mp.prepare();
                                    mp.start();
                                }
                                mp.setOnCompletionListener(null);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Create human-readable text for time period
     *
     * @param period period in milliseconds
     * @return
     */
    public String formatPeriod(long period) {
//        long hours = TimeUnit.MILLISECONDS.toHours(peroid);
        String periodFormatMask = getString(R.string.period_format_mask);
        Log.d(TAG, String.format("peroid111=%s", period));
        long days = period / 1000 / 24 / 60 / 60;
        period -= days * 1000 * 24 * 60 * 60;
        long hours = period / 1000 / 60 / 60;
        period -= hours * 1000 * 60 * 60;
        long minutes = period / 1000 / 60;
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd days, HH hours, mm minutes");
        SimpleDateFormat dateFormat = new SimpleDateFormat(periodFormatMask);
        Date date = new Date(period);
//        return  dateFormat.format(date)+"("+period+")";
//        return  dateFormat.format(peroid)+"("+period+")";
        return String.format("%s дней, %s часов, %s минут (%d) pva", days, hours, minutes, period);
    }

    public String makeNotificationText() {
        StringBuilder sb = new StringBuilder(200);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

        sb.append(currentParkingPoint.getName());
        sb.append(" ").append(currentParkingSide.getName());
        if (endTime != null) {

            sb.append("\nПарковка закончится ");
            sb.append(dateFormatter.format(endTime.getTime()));
            sb.append("\n(через ");
            sb.append(formatPeriod(period));
            sb.append(")\n");
        }
        if (currentLocation != null && currentLocation.getAccuracy() > 0) {
            sb.append("\nТочность местоположения ").
                append(currentLocation.getAccuracy()).
                append(" метров.");
        }
        return sb.toString();
    }
}

