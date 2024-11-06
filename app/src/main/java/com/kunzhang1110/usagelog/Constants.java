package com.kunzhang1110.usagelog;

import android.app.usage.UsageEvents;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

class Constants {

    static final Map<Integer, String> EVENT_TYPE_MAP = new HashMap<>();

    static {
        // These four types are used in calculate duration
        EVENT_TYPE_MAP.put(UsageEvents.Event.ACTIVITY_RESUMED, "Activity Resumed");
        EVENT_TYPE_MAP.put(UsageEvents.Event.ACTIVITY_STOPPED, "Activity Stopped");
        EVENT_TYPE_MAP.put(UsageEvents.Event.SCREEN_NON_INTERACTIVE, "Screen Non-Interactive");
        EVENT_TYPE_MAP.put(UsageEvents.Event.KEYGUARD_HIDDEN, "Keyguard Hidden");
        // The followings types are not used in calculate duration
        EVENT_TYPE_MAP.put(UsageEvents.Event.SCREEN_INTERACTIVE, "Screen Interactive");
        EVENT_TYPE_MAP.put(UsageEvents.Event.KEYGUARD_SHOWN, "Keyguard Shown");
        EVENT_TYPE_MAP.put(UsageEvents.Event.ACTIVITY_PAUSED, "Activity Paused");
        EVENT_TYPE_MAP.put(UsageEvents.Event.FOREGROUND_SERVICE_START, "Foreground Service Start");
        EVENT_TYPE_MAP.put(UsageEvents.Event.FOREGROUND_SERVICE_STOP, "Foreground Service Stop");
        EVENT_TYPE_MAP.put(UsageEvents.Event.DEVICE_STARTUP, "Device Startup");
        EVENT_TYPE_MAP.put(UsageEvents.Event.DEVICE_SHUTDOWN, "Device Shutdown");
        EVENT_TYPE_MAP.put(UsageEvents.Event.CONFIGURATION_CHANGE, "Configuration Change");
        EVENT_TYPE_MAP.put(UsageEvents.Event.SHORTCUT_INVOCATION, "Shortcut Invocation");
        EVENT_TYPE_MAP.put(UsageEvents.Event.USER_INTERACTION, "User Interaction");
    }

    static final ArrayList<String> EVENT_TYPES_FOR_DURATION_LIST = // These four types are used in calculate duration
            new ArrayList<>(
                    Arrays.asList("Activity Resumed", "Activity Stopped", "Screen Non-Interactive", "Keyguard Hidden"));
    static final ArrayList<String> APP_NAME_EXCLUDED_LIST = // These Apps are excluded from Event List
            new ArrayList<>((Arrays.asList("Permission controller", "Pixel Launcher", "Quickstep","System UI")));

    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static final int DAYS_OF_EVENTS_INCLUDED = 1; // days in which events are included

    static final long BEGIN_TIME_IN_MILLIS;
    static final Calendar CAL = Calendar.getInstance();
    static final LocalTime COPY_SESSION_START_TIME;
    static final LocalTime COPY_SESSION_END_TIME;


    static {
        COPY_SESSION_START_TIME = LocalTime.of(9, 0); //
        COPY_SESSION_END_TIME = LocalTime.of(22, 0); //
        CAL.add(Calendar.DATE, -DAYS_OF_EVENTS_INCLUDED);
        BEGIN_TIME_IN_MILLIS = CAL.getTimeInMillis();
    }

    static final int CONCISE_MIN_TIME_IN_SECONDS = 1200;// 20 min.
}
