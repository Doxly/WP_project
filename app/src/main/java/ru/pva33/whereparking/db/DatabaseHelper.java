package ru.pva33.whereparking.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.Calendar;

import ru.pva33.whereparking.R;

/**
 * Created by pva on 28.01.16.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "whereparking.db";

    // todo To upgrade mechanism works, don't forget incrise version number!!!
    private static final int DATABASE_VERSION = 2;

    private static final String TAG = "PVA_DEBUG";

    private Dao<ParkingPoint, Integer> parkingPontDao;
    private Dao<ParkingSide, Integer> parkingSideDao;
    private Dao<ParkingRestriction, Integer> parkingRestrictionDao;

    public DatabaseHelper(Context context) {
        // file ormlite_config used to avoid reflection usage at runtime and therefor increase performance
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    public Dao<ParkingPoint, Integer> getParkingPontDao() throws SQLException {
        if (parkingPontDao == null) {
            parkingPontDao = getDao(ParkingPoint.class);
        }
        return parkingPontDao;
    }

    public Dao<ParkingSide, Integer> getParkingSideDao() throws SQLException {
        if (parkingSideDao == null) {
            parkingSideDao = getDao(ParkingSide.class);
        }
        return parkingSideDao;
    }

    public Dao<ParkingRestriction, Integer> getParkingRestrictionDao() throws SQLException {
        if (parkingRestrictionDao == null) {
            parkingRestrictionDao = getDao(ParkingRestriction.class);
        }
        return parkingRestrictionDao;
    }

    /**
     * This method used for initial creation of database, tables and fill them with some data.
     *
     * @param database         db
     * @param connectionSource
     */
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, ParkingPoint.class);
            TableUtils.createTable(connectionSource, ParkingSide.class);
            TableUtils.createTable(connectionSource, ParkingRestriction.class);

            // Fill predefined data
            ParkingPoint pp = new ParkingPoint("Home", 53.204509, 50.16056);
            pp = getParkingPontDao().createIfNotExists(pp);

            ParkingSide ps1, ps2;
            ps1 = new ParkingSide(pp, "Сторона у дома");
            ps1 = getParkingSideDao().createIfNotExists(ps1);
            ps2 = new ParkingSide(pp, "Сторона напротив дома");
            ps2 = getParkingSideDao().createIfNotExists(ps2);

            ParkingRestriction pr;
            int hourFrom = 0, hourTo = 6;
            for (int day = Calendar.MONDAY; day <= Calendar.WEDNESDAY; day++) {
                pr = new ParkingRestriction(ps1, day, hourFrom, hourTo);
                getParkingRestrictionDao().createIfNotExists(pr);
            }
            for (int day = Calendar.THURSDAY; day <= Calendar.SATURDAY; day++) {
                pr = new ParkingRestriction(ps2, day, hourFrom, hourTo);
                getParkingRestrictionDao().createIfNotExists(pr);
            }
            Log.d(TAG, "Database created.");
        } catch (SQLException e) {
            Log.e(TAG, DatabaseHelper.class.getName() + ": Unable to create Database.");
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database,
                          ConnectionSource connectionSource,
                          int oldVersion,
                          int newVersion) {
        for (int upgradeTo = oldVersion + 1; upgradeTo <= newVersion; upgradeTo++) {
            String sql = "alter table " + ParkingPoint.class.getSimpleName() +
                " add column alert boolean";
            database.execSQL(sql);
        }

    }
}
