package com.kunzhang1110.eventlog.models;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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