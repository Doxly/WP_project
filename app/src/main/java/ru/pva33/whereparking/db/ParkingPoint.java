package ru.pva33.whereparking.db;

//import com.j256.orm

import com.google.android.gms.maps.model.LatLng;
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
public class ParkingPoint implements Serializable, SoundKeeper {
    @DatabaseField(canBeNull = false)
    String name;
    @DatabaseField(canBeNull = false)
    double latitude;      // south/nord
    @DatabaseField(canBeNull = false)
    double longitude;     // east/west
    @DatabaseField
    String soundPath;
    // added in version 2
    @DatabaseField
    boolean alert;
    @DatabaseField(generatedId = true, canBeNull = false)
    private Long _id;
    // eager true means that all sides would recieved with this object record_enable
    @ForeignCollectionField(eager = true)
    private Collection<ParkingSide> sides;

    public ParkingPoint() {
    }

    public ParkingPoint(String name, LatLng latLng) {
        this(name, latLng.latitude, latLng.longitude);
    }

    public ParkingPoint(String name, double latitude, double longitude) {
        super();
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean hasAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public String getSoundPath() {
        return soundPath;
    }

    public void setSoundPath(String soundPath) {
        this.soundPath = soundPath;
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

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Select side where there is no active restrictions right now and which have
     * maximum time before restriction.
     *
     * @param now
     * @return
     */
    public ParkingSide chooseParkingSide(Calendar now) {
        //

        ParkingSide ps = null;
        long timeBeforeRestriction = 0;
        for (Iterator<ParkingSide> i = getSides().iterator(); i.hasNext(); ) {
            ParkingSide loopSide = i.next();
            if (loopSide.isRetricted(now)) {
                continue;
            }
            long sideTime = loopSide.getTimeBefore(now);
            if (sideTime > timeBeforeRestriction) {
                ps = loopSide;
                timeBeforeRestriction = sideTime;
            }
        }
        return ps;
    }

    @Override
    public String toString() {
        return super.toString() + " [id=" + _id + ", name=" + name + ", soundPath=" + soundPath + ", longitude=" + longitude + ", laltitude=" + latitude + "]";
    }

    public String getSidesText() {
        StringBuilder sb = new StringBuilder();
        for (Iterator<ParkingSide> i = getSides().iterator(); i.hasNext(); ) {
            sb.append(i.next().getName()).append("\n");
        }
        return sb.toString();
    }

    public LatLng getPosition() {
        return new LatLng(getLatitude(), getLongitude());
    }

    public void setPosition(LatLng latLng) {
        setLatitude(latLng.latitude);
        setLongitude(latLng.longitude);
    }

}
