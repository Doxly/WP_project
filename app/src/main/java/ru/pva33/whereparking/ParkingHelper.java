package ru.pva33.whereparking;

import android.content.Context;

import java.io.File;

import ru.pva33.whereparking.db.ParkingPoint;
import ru.pva33.whereparking.db.ParkingSide;
import ru.pva33.whereparking.db.SoundKeeper;

/**
 * Tool for some common operations.
 * Created by pva on 11.02.16.
 */
public class ParkingHelper {

    public static final int EDIT_PP = 1;
    public static final int EDIT_PS = 2;
    public static final int EDIT_SOUND = 3;
    public static final int SHOW_PP = 4;
    public static final int SHOW_MAP = 5;

    /**
     * Generate file name for sound on the base of parking poing id and parking side id.
     * File name contain full absolute path and extention ".3gp"
     *
     * @param context
     * @param parkingPointId
     * @param parkingSideId
     * @return
     */
    public static String getSoundFileName(Context context, Long parkingPointId,
                                          Long parkingSideId) {

        String path = context.getExternalFilesDir(null).getAbsolutePath();
        path += "/" + parkingPointId + "_" + parkingSideId + ".3gp";
        return path;
    }

    /**
     * Generate file name for sound on the base of given {@link SoundKeeper}, which may be
     * either {@link ParkingPoint}, either {@link ParkingSide}.
     *
     * @param context
     * @param keeper
     * @return
     * @see #getSoundFileName(Context, Long, Long)
     */
    public static String getSoundFileName(Context context, SoundKeeper keeper) {
        Long parkingPointId = 0L;
        Long parkingSideId = 0L;
        if (keeper instanceof ParkingPoint) {
            ParkingPoint pp = (ParkingPoint) keeper;
            parkingPointId = pp.get_id();
            parkingSideId = 0L;
        } else if (keeper instanceof ParkingSide) {
            ParkingSide ps = (ParkingSide) keeper;
            parkingSideId = ps.get_id();
            parkingPointId = ps.getParkingPoint().get_id();
        }
        return getSoundFileName(context, parkingPointId, parkingSideId);
    }

    /**
     * Check if file really exists in device file system.
     *
     * @param mFileName
     * @return
     */
    public static boolean fileExists(String mFileName) {
        File file = new File(mFileName);
        return file.exists();
    }
}
