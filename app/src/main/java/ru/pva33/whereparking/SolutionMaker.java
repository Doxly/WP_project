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
 * Created by pva on 28.01.16.
 */
public class SolutionMaker {
    private Context context;
//    private ParkingPoint parkingPoint;
    private ParkingSide selectedParkingSide;
    private Calendar currentDate;
    private int supposedParkingDuration;

    public DatabaseHelper getDatabaseHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    private DatabaseHelper databaseHelper;
    private List<ParkingSide> parkingSides;

//    public SolutionMaker(ParkingPoint parkingPoint) {
    public SolutionMaker(Context context) {
//        this.parkingPoint = parkingPoint;
        this.context = context;
        this.currentDate = Calendar.getInstance();
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
    /*
    * На входе точка парковки
    * достаем стороны точки парковки
    * для каждой стороны парковки достаем список ее ограничений
    * определяем текущую дату-время
    * определяем по предполагаемой длительности стоянки дату-время окончания парковки
    * Проверяем для каждой стороны наличие запрета на сейчас. Если есть запрет - сторона отбрасывается.
    * для каждой из оставшихся сторон: определяем ближайшую дату время начала действия заперета
    * в качестве результата выдаем сторону у которой запрет наступет позднее.
    * для стороны результата выдаем ее саму, дату-время начала запрета и остаток времени до запрета.
    * */

    public void makeSolution() {
        // дата время окончания парковки
        Calendar parkingEnd = (Calendar) getCurrentDate().clone();
        parkingEnd.add(Calendar.HOUR, supposedParkingDuration);

    }

    /**
     * Choose parking side and remember maximum time in hours before any its
     * restriction starts. To see that time used {@link #getParkingDuration(ParkingPoint)}}
     * @return selected parking side
     */
    public ParkingSide chooseParkingSide(ParkingPoint pp){
        selectedParkingSide = pp.chooseParkingSide(this.currentDate);
        supposedParkingDuration = selectedParkingSide.getHoursBefore(this.currentDate);
        return selectedParkingSide;
    }

    /**
     * Return maximum duration in hours of parking on selected parking side.
     * If parking side is not selectes yet, it makes implicit coice for us.
     * @return
     */
    public int getParkingDuration(ParkingPoint pp){
        if (selectedParkingSide == null) {
            chooseParkingSide(pp);
        }
        return supposedParkingDuration;
    }

    /**
     * Return date-time when selected side becomes restrictid.
     * Acuracy of time calculation is one hour.
     * @return date-time
     * @see #getParkingDuration(ParkingPoint)
     */
    public Calendar getEndTime(ParkingPoint pp){
        Calendar endTime = (Calendar) getCurrentDate().clone();
        endTime.add(Calendar.HOUR, getParkingDuration(pp));
        return endTime;
    }

    /**
     * return date-time for notification as time prior parking on selected side ended.
     * @param minutes delta in minutes before end time of parking.
     * @return date-time
     * @see #getEndTime(ParkingPoint)
     * @see #getParkingDuration(ParkingPoint)
     * @see #chooseParkingSide(ParkingPoint)
     */
    public Calendar getNotificationTime(int minutes){
        Calendar notificationTime = (Calendar) getCurrentDate().clone();
        notificationTime.add(Calendar.MINUTE, -minutes);
        return notificationTime;
    }

    /**
     * find all {@link ParkingPoint} in circle whith center in given point and
     * radius given as distance.
     * @param latLng center of search circle
     * @param distance radius of search circle im meters
     * @return
     */
    public List<ParkingPoint> findNearPP(LatLng latLng, double distance) throws SQLException {
        List allPP = getDatabaseHelper().getParkingPontDao().queryForAll();
        return findNearPP(latLng, distance, allPP);
    }

    public List<ParkingPoint> findNearPP(LatLng latLng, double distance, List allPP){
        List resList = new ArrayList();
        for (Iterator i = allPP.iterator(); i.hasNext() ;) {
            ParkingPoint pp = (ParkingPoint) i.next();
            double ppDist = ParkingHelper.computeDistanceBetween(latLng, pp.getPosition());
            if (ppDist <= distance){
                resList.add(pp);
            }
        }
        return resList;
    }
}
