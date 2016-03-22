package ru.pva33.whereparking;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import ru.pva33.whereparking.db.DatabaseHelper;
import ru.pva33.whereparking.db.ParkingPoint;
import ru.pva33.whereparking.db.ParkingSide;

/**
 * It would make decision where parking now for some parking point.
 * Starting data is:<br>
 * - parking point<br>
 * - system date and time<br>
 * - supposed parking duration<br>
 * <p/>
 * Results:<br>- Side for parking
 * - maximal parking duration<br>
 * - Date-time when parking ends
 * Created by pva on 28.01.16.
 */
public class SolutionMaker {
    private static final String TAG = "PVA_DEBUG";
    private Context context;
    private ParkingSide selectedParkingSide;
    private Calendar currentDate;
    private Calendar endTime;
    private long supposedParkingDuration;
    // Parking duration in milliseconds
    private DatabaseHelper databaseHelper;

    //    public SolutionMaker(ParkingPoint parkingPoint) {
    public SolutionMaker(Context context) {
//        this.parkingPoint = parkingPoint;
        this.context = context;
        this.currentDate = Calendar.getInstance();
    }

    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    public Calendar getCurrentDate() {
        if (currentDate == null) {
            currentDate = Calendar.getInstance();
        }
        return currentDate;
    }

    /*
    Would used for unit testing
     */
    public void setCurrentDate(Calendar currentDate) {
        this.currentDate = currentDate;
    }

    /**
     * Choose parking side and remember maximum time in hours before any its
     * restriction starts. To see that time used {@link #getParkingDuration(ParkingPoint)}}
     *
     * @return selected parking side
     */
    public ParkingSide chooseParkingSide(ParkingPoint pp) {
        selectedParkingSide = pp.chooseParkingSide(getCurrentDate());
        if (selectedParkingSide != null) {
            supposedParkingDuration = selectedParkingSide.getTimeBefore(getCurrentDate());
            endTime = selectedParkingSide.getNextDateRestriction(getCurrentDate());
        }
        return selectedParkingSide;
    }

    /**
     * Return maximum duration in milliseconds of parking on selected parking side.
     * If parking side is not selectes yet, it makes implicit coice for us.
     *
     * @return
     */
    public long getParkingDuration(ParkingPoint pp) {
        if (selectedParkingSide == null) {
            chooseParkingSide(pp);
        }
        return supposedParkingDuration;
    }

    /**
     * Return date-time when selected side becomes restrictid.
     * Acuracy of time calculation is one hour.
     *
     * @return date-time
     * @see #getParkingDuration(ParkingPoint)
     */
    public Calendar getEndTime(ParkingPoint pp) {
        if (endTime == null) {
            chooseParkingSide(pp);
        }
        return endTime;
    }

    /**
     * return date-time for notification as time prior parking on selected side ended.
     * TODO now thats is wrong code
     *
     * @param minutes delta in minutes before end time of parking.
     * @return date-time
     * @see #getEndTime(ParkingPoint)
     * @see #getParkingDuration(ParkingPoint)
     * @see #chooseParkingSide(ParkingPoint)
     */
    public Calendar getNotificationTime(int minutes) {
        Calendar notificationTime = (Calendar) getCurrentDate().clone();
        notificationTime.add(Calendar.MINUTE, -minutes);
        return notificationTime;
    }

    /**
     * find all {@link ParkingPoint} in circle whith center in given point and
     * radius given as distance.
     *
     * @param latLng   center of search circle
     * @param distance radius of search circle im meters
     * @return
     */
    public List<ParkingPoint> findAllNearPP(LatLng latLng, double distance) throws SQLException {
        List allPP = getDatabaseHelper().getParkingPontDao().queryForAll();
        return findAllNearPP(latLng, distance, allPP);
    }

    public List<ParkingPoint> findAllNearPP(LatLng latLng, double distance, List allPP) {
        List resList = new ArrayList();
        for (Iterator i = allPP.iterator(); i.hasNext(); ) {
            ParkingPoint pp = (ParkingPoint) i.next();
            double ppDist = ParkingHelper.computeDistanceBetween(latLng, pp.getPosition());
            if (ppDist <= distance) {
                resList.add(pp);
            }
        }
        return resList;
    }

    /**
     * Find one of none {@link ParkingPoint} withing circle given by its center and radius
     * among list of given parking points.
     *
     * @param latLng   center of circle
     * @param distance radius of ciecle
     * @param allPP    list of parking points
     * @return
     */
    public ParkingPoint findNearestPP(LatLng latLng, double distance, List allPP) {
        List nearPPList = findAllNearPP(latLng, distance, allPP);
        ParkingPoint res = null;
//        Log.d(TAG,
//            String.format("SolutionMaker.findNerestPP(latLng=%s, distance=%s, allPP=%s", latLng,
//                distance, allPP
//            )
//        );
        double minDistance = distance + 1;
        for (Iterator i = nearPPList.iterator(); i.hasNext(); ) {
            ParkingPoint pp = (ParkingPoint) i.next();
            double dist = ParkingHelper.computeDistanceBetween(latLng, pp.getPosition());
            if (minDistance >= dist) {
                minDistance = dist;
                res = pp;
            }
        }
        return res;
    }
}
