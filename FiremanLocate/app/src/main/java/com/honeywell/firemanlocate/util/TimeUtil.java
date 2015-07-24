package com.honeywell.firemanlocate.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lynnliu on 7/16/15.
 */
public class TimeUtil {

    public static Object[] getTimestamp() {
        Object timeDiff[] = new Object[2];
        int timeDiff_second = 0;
        short timeDiff_millisecond = 0;
        try {
            long currentTime = System.currentTimeMillis();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date baselineDate = df.parse("2015-01-01 00:00:00");
            long timeDiff_total = currentTime - baselineDate.getTime();
            timeDiff_second = (int) (timeDiff_total / 1000);
            timeDiff_millisecond = (short) (timeDiff_total % 1000);
            timeDiff[0] = timeDiff_second;
            timeDiff[1] = timeDiff_millisecond;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeDiff;
    }
}
