package com.kunzhang1110.usagelog.models;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppModel implements Comparable<AppModel> {

    public String appName;
    public Drawable appIcon;
    public LocalDateTime time;
    public final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AppModel() {
    }
    public AppModel(String appName, Drawable appIcon, LocalDateTime time) {
        this.appName = appName;
        this.appIcon = appIcon;
        this.time = time;
    }

    @Override
    public int compareTo(AppModel otherRowData) {
        return time.compareTo(otherRowData.time);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format("%s, %s", appName, time.format(dateTimeFormatter));
    }
}
