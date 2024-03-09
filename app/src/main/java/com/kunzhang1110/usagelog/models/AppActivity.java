package com.kunzhang1110.usagelog.models;

import android.graphics.drawable.Drawable;

import java.time.LocalDateTime;

public class AppActivity extends AppModel {

    public Long durationInSeconds = 0L;
    public String durationInText;

    public AppActivity() {
        super();
    }

    public AppActivity(String appName, Drawable appIcon, LocalDateTime time, Long durationInSeconds) {
        super(appName, appIcon, time);
        this.durationInSeconds = durationInSeconds;

        Long seconds = durationInSeconds % 60;
        Long minutes = (durationInSeconds / 60) % 60;
        Long hours = (durationInSeconds / (60 * 60));
        durationInText = String.format("%sh %sm %ss", hours, minutes, seconds) ;

        this.durationInText = durationInText;
    }


}
