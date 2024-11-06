package com.kunzhang1110.usagelog.models;

import android.graphics.drawable.Drawable;

import java.time.LocalDateTime;

public class AppEvent extends AppModel {

    public String eventType;

    public AppEvent() {
        super();
    }

    public AppEvent(String appName, Drawable appIcon, LocalDateTime time, String eventType) {
        super(appName, appIcon, time);
        this.eventType = eventType;
    }


}