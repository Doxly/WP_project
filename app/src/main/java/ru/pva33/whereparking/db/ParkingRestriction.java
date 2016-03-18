package ru.pva33.whereparking.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Records about restriction for parking and there conditions
 * Created by pva on 28.01.16.
 */
@DatabaseTable
public class ParkingRestriction implements Serializable {


    private static final String TAG = "PVA_DEBUG.PR";
    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
    ParkingSide parkingSide;
    @DatabaseField
    int dayOfWeek;
    @DatabaseField
    int hourFrom;
    @DatabaseField
    int hourTo;
    @DatabaseField(generatedId = true, canBeNull = false)
    private Long _id;

    public ParkingRestriction() {
    }

    public ParkingRestriction(ParkingSide parkingSide, int dayOfWeek, int hourFrom, int hourTo) {
        super();
        this.parkingSide = parkingSide;
        this.dayOfWeek = dayOfWeek;
        this.hourFrom = hourFrom;
        this.hourTo = hourTo;

    }

    @Override
    public String toString() {
        return "Restriction[dayOfWeek=" + this.dayOfWeek + ",hourFrom=" + this.hourFrom + ",hourTo=" + this.hourTo + "]";
    }

    /**
     * Return true if this restriction is artive in given date-time
     *
     * @param calendar
     * @return
     */
    public boolean isActive(Calendar calendar) {
        int hour = calendar.get(calendar.HOUR_OF_DAY);
        boolean result = calendar.get(calendar.DAY_OF_WEEK) == this.dayOfWeek
                && this.hourFrom <= hour
                && hour < this.hourTo;
//        return true;
//        Log.d("PVA_DEBUG", "is active: calendar="+calendar+" this="+toString());
        return result;
    }

    /**
     * How many hours before this restriction would become active
     *
     * @param calendar
     * @return
     */
    public int getHoursBefore(Calendar calendar) {
        int result = 0;
        if (this.isActive(calendar)) {
            return result;
        }
        int current_day_of_week = calendar.get(calendar.DAY_OF_WEEK);
        int current_hour = calendar.get(calendar.HOUR_OF_DAY);
        int delta = this.dayOfWeek - current_day_of_week;
        delta = delta < 0 ? delta + 7 : delta;
        result = delta * 24 + (this.hourFrom - current_hour);
        result = result < 0 ? result + 24 * 7 : result;
        return result;
    }

    /**
     * Get next date after given when restriction would becomes active
     * @param calendar
     * @return
     */
    public Calendar getNextDate(Calendar calendar){
        /* Get target day or week as dx,
        get current day of week as dNow,
        calc delta as delta=dx-dnow.
        if delta <0 then delta=delta+7
        add delta to given current date, remove current time (replace it with restriction hours)
        * */
        int dNow = calendar.get(Calendar.DAY_OF_WEEK);
//        Log.d(TAG,
//            String.format("GetNextDate calendar=%s, dNow=%s, this dayOfWeek=%s", calendar, dNow,
//                dayOfWeek
//            )
//        );
        int delta = this.dayOfWeek - dNow;
        delta = delta < 0 ? delta + 7 : delta;
        if (this.hourFrom == 0 && delta == 0){
            delta = 7;
        }
//        Log.d(TAG, String.format("delta=%s", delta));
        Calendar result = (Calendar) calendar.clone();
        result.clear(Calendar.HOUR); // clear all time fields
        result.clear(Calendar.HOUR_OF_DAY);
        result.clear(Calendar.MINUTE);
        result.clear(Calendar.SECOND);
        result.clear(Calendar.MILLISECOND);
        result.add(Calendar.DAY_OF_MONTH, delta);
        result.add(Calendar.HOUR_OF_DAY, this.hourFrom);
        return result;
    }
}
