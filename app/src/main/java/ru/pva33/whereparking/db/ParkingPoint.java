package ru.pva33.whereparking.db;

//import com.j256.orm

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

/**
 * Presents one place where we doubt about parking side.
 * It has been binded to geolocation.
 * Created by pva on 28.01.16.
 */
@DatabaseTable
public class ParkingPoint implements Serializable {
    @DatabaseField(canBeNull = false)
    String name;
    @DatabaseField(canBeNull = false)
    double latitude;      // south/nord
    @DatabaseField(canBeNull = false)
    double longitude;     // east/west
    @DatabaseField(generatedId = true, canBeNull = false)
    private Long _id;
    // eager true means that all sides would recieved with this object record
    @ForeignCollectionField(eager = true)
    private Collection<ParkingSide> sides;

    public ParkingPoint() {
    }


    public ParkingPoint(String name, double latitude, double longitude) {
        super();
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Collection<ParkingSide> getSides() {
        if (sides == null) {
            sides = new ArrayList<>();
        }
        return sides;
    }

    public Long get_id() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public ParkingSide chooseParkingSide(Calendar calendar) {
        // select side where there is no active restrictions and which have maximum time before restriction
        ParkingSide ps = null;
        int timeBeforeRestriction = 0;
        for (Iterator<ParkingSide> i = getSides().iterator(); i.hasNext(); ) {
            ParkingSide loopSide = i.next();
            if (loopSide.isRetricted(calendar)) {
                continue;
            }
            int sideTime = loopSide.getHoursBefore(calendar);
            if (sideTime > timeBeforeRestriction) {
                ps = loopSide;
                timeBeforeRestriction = sideTime;
            }
        }
        return ps;
    }

}
