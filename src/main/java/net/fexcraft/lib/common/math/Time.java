package net.fexcraft.lib.common.math;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

public class Time {

    public static final long DAY_MS = 86400000;
    public static final long MIN_MS = 60000;


    public static int getSecond(){
        return LocalTime.now().getSecond();
    }
    public static long getDate(){return LocalDate.now().getDayOfMonth();}
    public static int getDay(){
        return LocalDate.now().getDayOfMonth();
    }

    public static final String getAsString(long date){
        return new SimpleDateFormat("dd|MM|yyyy HH:mm:ss").format(date >= 0 ? new Date(date) : new Date());
    }

}
