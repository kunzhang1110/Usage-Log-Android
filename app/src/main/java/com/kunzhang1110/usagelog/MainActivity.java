package com.kunzhang1110.usagelog;

import static com.kunzhang1110.usagelog.Constants.APP_NAME_EXCLUDED_LIST;

import static com.kunzhang1110.usagelog.Constants.BEGIN_TIME_IN_MILLIS;
import static com.kunzhang1110.usagelog.Constants.CONCISE_MIN_TIME_IN_SECONDS;
import static com.kunzhang1110.usagelog.Constants.DATE_TIME_FORMATTER;
import static com.kunzhang1110.usagelog.Constants.EVENT_TYPES_FOR_DURATION_LIST;
import static com.kunzhang1110.usagelog.Constants.EVENT_TYPE_MAP;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kunzhang1110.usagelog.models.AppEvent;
import com.kunzhang1110.usagelog.models.AppModel;
import com.kunzhang1110.usagelog.models.AppActivity;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private UsageStatsManager usageStatsManager;
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button btnConcise, btnAll, btnRaw;
    private final ArrayList<AppEvent> appEvents = new ArrayList<>();
    private final ArrayList<AppActivity> appActivities = new ArrayList<>();

    private String currentPressedTab = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listAdapter = new ListAdapter(this, DATE_TIME_FORMATTER);
        usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        recyclerView = findViewById(R.id.app_usage_list);
        recyclerView.setAdapter(listAdapter);

        swipeRefreshLayout = findViewById(R.id.layout_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
                    updateList();
                    switch (currentPressedTab) {
                        case "Concise":
                            btnConcise.callOnClick();
                            break;
                        case "All":
                            btnAll.callOnClick();
                            break;
                        case "Raw":
                            btnRaw.callOnClick();
                            updateAdapter(appEvents);
                            break;
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
        );

        btnConcise = findViewById(R.id.btn_concise);
        btnAll = findViewById(R.id.btn_all);
        btnRaw = findViewById(R.id.btn_raw);


        btnConcise.setOnClickListener(v -> {//only show each Screen Locked that is longer than x min. and the activity before it
            ArrayList<AppActivity> list = new ArrayList<>();
            for (int i = 1; i < appActivities.size(); i++) {
                AppActivity appActivity = appActivities.get(i);
                if (appActivity.appName.equals("Screen Locked") & (appActivity.durationInSeconds >= CONCISE_MIN_TIME_IN_SECONDS)) {
                    list.add(appActivities.get(i - 1));
                    list.add(appActivity);
                }
            }
            updateAdapter(list);
            highlightButton(btnConcise);
            currentPressedTab = "Concise";
        });
        btnAll.setOnClickListener(v -> {
            updateAdapter(appActivities);
            highlightButton(btnAll);
            currentPressedTab = "All";
        });
        btnRaw.setOnClickListener(v -> {
            updateAdapter(appEvents);
            highlightButton(btnRaw);
            currentPressedTab = "Raw";

        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> recyclerView.scrollToPosition(0));
        updateList();
        btnConcise.callOnClick(); //click btn concise
    }


    private void updateList() {

        UsageEvents usageEvents = usageStatsManager.queryEvents(BEGIN_TIME_IN_MILLIS, System.currentTimeMillis());

        Map<String, ArrayList<AppEvent>> appNameToAppEventMap = new HashMap<>();
        appActivities.clear();
        appEvents.clear();

        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);

            String packageName = event.getPackageName();
            String eventType = EVENT_TYPE_MAP.get(event.getEventType());
            if (eventType == null || packageName == null) continue;

            AppEvent appEvent = new AppEvent(
            );
            appEvent.eventType = eventType;
            appEvent.time = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()), ZoneId.systemDefault());

            try {//get app name
                PackageManager packageManager = getPackageManager();
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                String appName = (String) packageManager.getApplicationLabel(appInfo);
                if (APP_NAME_EXCLUDED_LIST.contains(appName)) continue;
                appEvent.appName = appName;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w("MyLog", String.format("App info is not found for %s", packageName));
                appEvent.appName = packageName;
            }

            try {//get app icon
                appEvent.appIcon = getPackageManager().getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w("MyLog", String.format("App Icon is not found for %s", packageName));
                appEvent.appIcon = AppCompatResources.getDrawable(this, android.R.drawable.sym_def_app_icon);
            }

            //add to eventsMap
            if (EVENT_TYPES_FOR_DURATION_LIST.contains(eventType)) {
                appNameToAppEventMap.computeIfAbsent(appEvent.appName, k -> new ArrayList<>()).add(appEvent);
            }
            //add to appEvents list
            appEvents.add(appEvent);
        }

        for (Map.Entry<String, ArrayList<AppEvent>> entry : appNameToAppEventMap.entrySet()) {
            String appName = entry.getKey();
            ArrayList<AppEvent> events = entry.getValue();

            for (int x = 0; x < events.size(); x++) {
                AppEvent eventX = events.get(x);

                if (isResumedOrNonInteractive(eventX)) {
                    int y = x + 1;

                    while (y < events.size() && isResumedOrNonInteractive(events.get(y))) {
                        y++;
                    }

                    if (y < events.size()) {
                        AppEvent eventY = events.get(y);
                        long durationInSeconds = Duration.between(eventX.time, eventY.time).toMillis() / 1000;
                        if (durationInSeconds > 0) {
                            String name = appName.equals("Android System") ? "Screen Locked" : appName;
                            AppActivity appActivity = new AppActivity(
                                    name, eventX.appIcon, eventX.time, durationInSeconds
                            );
                            appActivity.durationInSeconds = durationInSeconds;
                            appActivities.add(appActivity);
                            x = y;
                        }
                    }
                }
            }
        }

        Collections.sort(appActivities);
        Collections.reverse(appActivities);
        Collections.reverse(appEvents); //rawRowDataList is already in order
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateAdapter(ArrayList<? extends AppModel> appModels) {

        listAdapter.setData(appModels);
        listAdapter.notifyDataSetChanged();
    }

    private void highlightButton(Button button) {
        int btnPrimaryColor = getColor(R.color.md_theme_light_primary);
        int btnOnColor = getColor(R.color.md_theme_light_onPrimary);

        for (Button b : Arrays.asList(btnAll, btnConcise, btnRaw)) {
            b.setBackgroundColor(b.equals(button) ? btnPrimaryColor : btnOnColor);
            b.setTextColor(b.equals(button) ? btnOnColor : btnPrimaryColor);
        }
    }

    private boolean isResumedOrNonInteractive(AppEvent event) {
        return event.eventType.equals("Activity Resumed") || event.eventType.equals("Screen Non-Interactive");
    }

    private boolean isStoppedOrKeyguardHidden(AppEvent event) {
        return event.eventType.equals("Activity Stopped") || event.eventType.equals("Keyguard Hidden");
    }
}

