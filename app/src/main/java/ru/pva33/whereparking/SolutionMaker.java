package ru.pva33.whereparking;

import java.util.Calendar;
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
    private ParkingPoint parkingPoint;
    private Calendar currentDate;
    private int supposedParkingDuration;
    private DatabaseHelper databaseHelper;
    private List<ParkingSide> parkingSides;

    public SolutionMaker(ParkingPoint parkingPoint, Calendar currentDate,
                         int supposedParkingDuration) {
        this.parkingPoint = parkingPoint;
        this.currentDate = currentDate;
        this.supposedParkingDuration = supposedParkingDuration;
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
}
