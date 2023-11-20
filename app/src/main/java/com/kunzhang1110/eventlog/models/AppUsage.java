package com.kunzhang1110.eventlog.models;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppUsage extends AppModel {

    public Long durationInSeconds = 0L;
    public String durationInText;

    public AppUsage() {
        super();
    }

    public AppUsage(String appName, Drawable appIcon, LocalDateTime time, Long durationInSeconds) {
        super(appName, appIcon, time);
        this.durationInSeconds = durationInSeconds;

        Long seconds = durationInSeconds % 60;
        Long minutes = (durationInSeconds / 60) % 60;
        Long hours = (durationInSeconds / (60 * 60));
        durationInText = String.format("%sh %sm %ss", hours, minutes, seconds) ;

        this.durationInText = durationInText;
    }


}
