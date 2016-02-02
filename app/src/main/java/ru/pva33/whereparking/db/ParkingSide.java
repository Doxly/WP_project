package ru.pva33.whereparking.db;

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
public class ParkingSide implements Serializable {

    private static final String TAG = "PVA_DEBUG";
    @DatabaseField
    String allowText;
    @DatabaseField
    String allowSoundPath;
    @DatabaseField(generatedId = true, canBeNull = false)
    private Long _id;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false)
    private ParkingPoint parkingPoint;
    @DatabaseField(canBeNull = false)
    private String name;
    @ForeignCollectionField(eager = true)
    private Collection<ParkingRestriction> restrictions;

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
            restrictions = new ArrayList<ParkingRestriction>();
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

    public String getAllowSoundPath() {
        return allowSoundPath;
    }

    public void setAllowSoundPath(String allowSoundPath) {
        this.allowSoundPath = allowSoundPath;
    }

    /**
     * is any side restriction active for given date-time
     *
     * @param calendar
     * @return
     */
    public boolean isRetricted(Calendar calendar) {
        boolean result = false;
        for (Iterator<ParkingRestriction> i = getRestrictions().iterator(); i.hasNext(); ) {
            ParkingRestriction pr = i.next();
            result = pr.isActive(calendar);
            if (result) {
                break;
            }
        }
        return result;
    }

    public int getHoursBefore(Calendar calendar) {
        int result = 99999;
        // find restriction with smallest time before
        for (Iterator<ParkingRestriction> i = getRestrictions().iterator(); i.hasNext(); ) {
            int hb = i.next().getHoursBefore(calendar);
            result = result > hb ? hb : result;
        }
        return result;
    }

    public String getName() {
        return name;
    }
}
