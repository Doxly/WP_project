package ru.pva33.whereparking;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ru.pva33.whereparking.db.ParkingPoint;
import ru.pva33.whereparking.db.ParkingRestriction;
import ru.pva33.whereparking.db.ParkingSide;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    private static final String TAG = "PVA_DEBUG";

    public ApplicationTest() {
        super(Application.class);
    }

    public void testParkingRestrictionIsActive() {
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        ParkingRestriction pr = new ParkingRestriction(null, dayOfWeek, hour, hour + 1);
//        Log.d(TAG, "is active test pr=" + pr);
        assertTrue(pr.isActive(now));
        pr = new ParkingRestriction(null, dayOfWeek, hour + 1, hour + 2);
        assertFalse(pr.isActive(now));
        pr = new ParkingRestriction(null, dayOfWeek + 1, hour, hour + 1);
        assertFalse(pr.isActive(now));
    }

    public void testParkingRestrictionGetHoursBefore() {
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int hour = now.get(Calendar.HOUR_OF_DAY);

        ParkingRestriction pr = new ParkingRestriction(null, dayOfWeek, hour, hour + 1);
        assertEquals(0, pr.getHoursBefore(now));

        pr = new ParkingRestriction(null, dayOfWeek, hour + 3, hour + 4);
        assertEquals(3, pr.getHoursBefore(now));

        pr = new ParkingRestriction(null, dayOfWeek + 1, hour + 3, hour + 4);
        assertEquals(24 + 3, pr.getHoursBefore(now));

        pr = new ParkingRestriction(null, dayOfWeek, hour - 2, hour - 1);
        assertEquals(24 * 7 - 2, pr.getHoursBefore(now));

        pr = new ParkingRestriction(null, dayOfWeek - 1, hour, hour + 1);
        assertEquals(24 * 6, pr.getHoursBefore(now));
    }

    public void testParkingSideIsRestricted() {
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int hour = now.get(Calendar.HOUR_OF_DAY);

        ParkingSide ps = new ParkingSide(null, "test parking side");
        ParkingRestriction pr = new ParkingRestriction(null, dayOfWeek - 1, hour, hour + 1);
        ps.getRestrictions().add(pr);
        assertFalse(ps.isRetricted(now));

        pr = new ParkingRestriction(null, dayOfWeek + 1, hour, hour + 1);
        ps.getRestrictions().add(pr);
        assertFalse(ps.isRetricted(now));

        pr = new ParkingRestriction(null, dayOfWeek, hour - 2, hour - 1);
        ps.getRestrictions().add(pr);
        assertFalse(ps.isRetricted(now));

        pr = new ParkingRestriction(null, dayOfWeek, hour + 1, hour + 2);
        ps.getRestrictions().add(pr);
        assertFalse(ps.isRetricted(now));

        pr = new ParkingRestriction(null, dayOfWeek, hour, hour + 1);
        ps.getRestrictions().add(pr);
        assertTrue(ps.isRetricted(now));
    }

    public void testParkingSideGetHoursBefore() {
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int hour = now.get(Calendar.HOUR_OF_DAY);
//        Log.d(TAG, "DayOfWeek=" + dayOfWeek + " hour=" + hour);
        ParkingSide ps = new ParkingSide(null, "test parking side");
        long maxTime;

        // tomorrow at the same time
        ParkingRestriction pr = new ParkingRestriction(null, dayOfWeek + 1, hour, hour + 1);
        ps.getRestrictions().add(pr);

        // time in milliseconds
        maxTime = ps.getTimeBefore(now);
        assertEquals(24 - 1, maxTime/60/60/1000);

        // tooday 5 hours later
        ps.getRestrictions().add(new ParkingRestriction(null, dayOfWeek, hour + 5, hour + 6));
        maxTime = ps.getTimeBefore(now);
        assertEquals(5 - 1, maxTime/60/60/1000);
    }

    public void testParkingRestrictionGetNextDate(){
        Calendar now = Calendar.getInstance();
        // set to 01.01.2016 00:00:00 that is friday
        now.set(2016, 1,1,0,0,0);
        ParkingRestriction pr = new ParkingRestriction(null, Calendar.THURSDAY, 3, 6);
        Calendar next = pr.getNextDate(now);
        Log.d(TAG, String.format("parking restriction next date for thursday=%s", next));
        assertEquals(Calendar.THURSDAY, next.get(Calendar.DAY_OF_WEEK));
//        Log.d(TAG, String.format("next hour of day=%s, hour=%s", next.get(Calendar.HOUR_OF_DAY), next.get(Calendar.HOUR)));
        assertEquals(3, next.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, next.get(Calendar.MINUTE));
        assertTrue(next.after(now));
    }

    public void testParkingPointChooseParkingSide() {
        /* Choose side with MAXIMUN time before any of its restriction becomes active*/
        Calendar now = Calendar.getInstance();
        int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int hour = now.get(Calendar.HOUR_OF_DAY);

        ParkingPoint pp = new ParkingPoint("test parking point", 0, 0);
        // single side
        ParkingSide ps = new ParkingSide(pp, "first side");
        pp.getSides().add(ps);
        // Tooday, 1 hour later
        ps.getRestrictions().add(new ParkingRestriction(ps, dayOfWeek, hour + 1, hour + 2));
        ParkingSide result = pp.chooseParkingSide(now);
        assertEquals(ps, result);

        // second side with active restriction
        ps = new ParkingSide(pp, "wrong side");
        pp.getSides().add(ps);
        // right now
        ps.getRestrictions().add(new ParkingRestriction(ps, dayOfWeek, hour, hour + 1));
        result = pp.chooseParkingSide(now);
        assertNotNull(result);
        assertEquals("first side", result.getName());

        // third /yes why no?/ that is most suitable for parking
        ps = new ParkingSide(pp, "third side");
        pp.getSides().add(ps);
        // tomorrow at the same time
        ps.getRestrictions().add(new ParkingRestriction(ps, dayOfWeek + 1, hour, hour + 1));
        result = pp.chooseParkingSide(now);
        assertNotNull(result);
//        Log.d(TAG, "testParkingPointChooseParkingSide. side=" + result.getName());
        assertEquals("third side", result.getName());
    }

    public void testComputeDistance(){
        LatLng p1, p2;
        double distance;
        p1 = new LatLng(53.187131, 50.149610); // work
        p2 = new LatLng(53.204569, 50.160886); // home
        // expect 2081.86 m
        distance = ParkingHelper.computeDistanceBetween(p1, p2);
        // form with three parameters: expected, real, delta
        assertEquals(2081.86, distance, 1);
    }

    public void testFindNearPP(){
        LatLng center = new LatLng(50, 50);
        LatLng near = new LatLng(50.00001, 50);
        LatLng far = new LatLng(33,33);
        double dist = 3000; // in meters
        List ppList = new ArrayList();
        SolutionMaker solutionMaker = new SolutionMaker(null);

        ppList.add(new ParkingPoint("pp1", near));
        List resList = solutionMaker.findAllNearPP(center, dist, ppList);
        assertEquals(1, resList.size());

        ppList.add(new ParkingPoint("pp2", far));
        resList = solutionMaker.findAllNearPP(center, dist, ppList);
        assertEquals(1, resList.size());

        ppList.add(new ParkingPoint("pp3", near));
        resList = solutionMaker.findAllNearPP(center, dist, ppList);
        assertEquals(2, resList.size());

    }
}