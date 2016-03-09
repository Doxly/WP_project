package ru.pva33.whereparking.db;

/**
 * Interface for providing access to soundPath property.
 * Known realizations are: {@link ParkingPoint} and {@link ParkingSide}.
 * <p/>
 * Created by pva on 10.02.16.
 */
public interface SoundKeeper {
    String getSoundPath();

    void setSoundPath(String soundPath);
}
