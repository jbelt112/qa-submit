package qars.util;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Creates a timestamp in the format for release documentation.
 */
public class TimeStamp {
    private static final SimpleDateFormat sdf = 
        new SimpleDateFormat("MM-dd-yy::kk:mm:ss");
    
    /**
     * Like punching a timeclock at work, this method returns the current time
     * in the format 99-99-99::99:99:99 (month-day-year::hour:minute:second).
     * @param sb A StringBuffer where the time will be stored.
     * @return This method also returns a StringBuffer containing the time.
     */
    public static StringBuffer punch(StringBuffer sb) {
        return sdf.format(new Date(), sb, new FieldPosition(0));
    }
}