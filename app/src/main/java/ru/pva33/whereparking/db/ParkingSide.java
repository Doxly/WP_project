package ru.pva33.whereparking.db;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

/**
 * Single side for parking. It has its own name and restriction schedule.
 * Created by pva on 28.01.16.
 */

@DatabaseTable
public class ParkingSide implements Serializable, SoundKeeper {

    private static final String TAG = "PVA_DEBUG";
    @DatabaseField
    String allowText;
    @DatabaseField
    String allowSoundPath;
    @DatabaseField(generatedId = true, canBeNull = false)
    private Long _id;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
    private ParkingPoint parkingPoint;

    public void setName(String name) {
        this.name = name;
    }

    @DatabaseField(canBeNull = false)
    private String name;
    @ForeignCollectionField(eager = true)
    private Collection<ParkingRestriction> restrictions;

    public static final long INFINITY_PERIOD = 1000*60*60*24*365;

    public ParkingSide(ParkingPoint parkingPoint, String name) {
        super();
        this.parkingPoint = parkingPoint;
        this.name = name;
        this.allowText = "Можно парковаться";
    }

    public ParkingSide() {
    }

    public Collection<ParkingRestriction> getRestrictions() {
        // Не знаю насколько корректно здесь инициализировать это поле. Мне нужно для тестирования.
        if (restrictions == null) {
            restrictions = new ArrayList<>();
        }
        return restrictions;
    }

    public String getAllowText() {
        if (allowText == null) {
            return this.name;
        }
        return allowText;
    }

    public void setAllowText(String allowText) {
        this.allowText = allowText;
    }

    public ParkingPoint getParkingPoint() {
        return parkingPoint;
    }

    public Long get_id() {

        return _id;
    }

    public String getSoundPath() {
        return allowSoundPath;
    }

    public void setSoundPath(String allowSoundPath) {
        this.allowSoundPath = allowSoundPath;
    }

    /**
     * is any side restriction active for given date-time
     *
     * @param now
     * @return
     */
    public boolean isRetricted(Calendar now) {
        boolean result = false;
        for (Iterator<ParkingRestriction> i = getRestrictions().iterator(); i.hasNext(); ) {
            ParkingRestriction pr = i.next();
            result = pr.isActive(now);
            if (result) {
                break;
            }
        }
        return result;
    }

    /**
     * Get minimum time in milliseconds before any restriction becomes active
     *
     * @param now
     * @return
     */
    public long getTimeBefore(Calendar now) {
        long result;
        Calendar nextDate = getNextDateRestriction(now);
        if (nextDate == null) {
            return INFINITY_PERIOD;
        }
        if (now == null) {
            Log.e(TAG, "now is null");
            return 0;
        }
        result = nextDate.getTimeInMillis() - now.getTimeInMillis();
        return result;
    }

    public String getName() {
        return name;
    }

    public Calendar getNextDateRestriction(Calendar now) {
        Calendar result = null;
        // find restriction with smallest date
        for (Iterator<ParkingRestriction> i = getRestrictions().iterator(); i.hasNext(); ) {
            Calendar rDate = i.next().getNextDate(now);
            if (result == null || result.after(rDate)) {
                result = rDate;
            }
        }
        return result;
    }
}
