package com.kunzhang1110.usagelog;

import com.kunzhang1110.usagelog.models.AppModel;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

class Utils {
    // rounded to the nearest five minute
    public static String getRoundedTimeString(LocalDateTime dateTime) {
        int minutes = dateTime.getMinute();
        int roundedMinutes = (minutes / 5) * 5;
        return dateTime.withMinute(roundedMinutes).format(DateTimeFormatter.ofPattern("HHmm"));
    }

    // Gets Event Time Text in HHMMHHMM
    public static String getAppModelTimeText(List<? extends AppModel> data, int index) {
        if (index < data.size() - 1) {
            // if the time difference between this event and previous event time is less
            // than 5 minutes
            Duration duration = Duration.between(data.get(index + 1).time, data.get(index).time);
            if (duration.toMinutes() < 5) {
                // add five minutes toe this event start time
                data.get(index).time = data.get(index).time.plusMinutes(5);
            }
        }
        String startTimeText = getRoundedTimeString(data.get(index).time);
        String endTimeText = getRoundedTimeString(data.get(index - 1).time);

        return startTimeText + endTimeText;
    }
}
