package com.kunzhang1110.usagelog;

import static com.kunzhang1110.usagelog.Constants.APP_NAME_EXCLUDED_LIST;

import static com.kunzhang1110.usagelog.Constants.BEGIN_TIME_IN_MILLIS;
import static com.kunzhang1110.usagelog.Constants.CONCISE_MIN_TIME_IN_SECONDS;
import static com.kunzhang1110.usagelog.Constants.COPY_SESSION_END_TIME;
import static com.kunzhang1110.usagelog.Constants.COPY_SESSION_START_TIME;
import static com.kunzhang1110.usagelog.Constants.DATE_TIME_FORMATTER;
import static com.kunzhang1110.usagelog.Constants.EVENT_TYPES_FOR_DURATION_LIST;
import static com.kunzhang1110.usagelog.Constants.EVENT_TYPE_MAP;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kunzhang1110.usagelog.models.AppEvent;
import com.kunzhang1110.usagelog.models.AppModel;
import com.kunzhang1110.usagelog.models.AppUsage;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @noinspection SuspiciousNameCombination
 */
public class MainActivity extends AppCompatActivity {

    private UsageStatsManager usageStatsManager;
    private RecyclerView recyclerView;
    private ListAdapter listAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button btnConcise, btnAll, btnRaw;
    private ImageButton btnCopy;
    private final ArrayList<AppEvent> appEvents = new ArrayList<>();
    private final ArrayList<AppUsage> appUsages = new ArrayList<>();
    private final ArrayList<AppUsage> appConciseUsages = new ArrayList<>();
    private String currentPressedTab = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (!hasUsageAccessPermission()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        } else {
            init();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init() {
        listAdapter = new ListAdapter(this, DATE_TIME_FORMATTER);
        usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        recyclerView = findViewById(R.id.app_usage_list);
        recyclerView.setAdapter(listAdapter);

        swipeRefreshLayout = findViewById(R.id.layout_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            updateData();
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
        });

        btnConcise = findViewById(R.id.btn_concise);
        btnAll = findViewById(R.id.btn_all);
        btnRaw = findViewById(R.id.btn_raw);
        btnCopy = findViewById(R.id.btn_copy);

        btnConcise.setOnClickListener(v -> {// only show each Screen Locked that is longer than x min. and the activity
            // before it
            updateAdapter(appConciseUsages);
            highlightButton(btnConcise);
            btnCopy.setVisibility(View.VISIBLE);
            currentPressedTab = "Concise";
        });
        btnAll.setOnClickListener(v -> {
            updateAdapter(appUsages);
            highlightButton(btnAll);
            btnCopy.setVisibility(View.GONE);
            currentPressedTab = "All";
        });
        btnRaw.setOnClickListener(v -> {
            updateAdapter(appEvents);
            highlightButton(btnRaw);
            btnCopy.setVisibility(View.GONE);
            currentPressedTab = "Raw";
        });

        btnCopy.setOnClickListener(v -> {
            // copy all event times that are between [COPY_SESSION_START_TIME] and [COPY_SESSION_END_TIME] onto clipboard.
            List<String> copyText = new ArrayList<>();


            LocalDateTime firstStartDateTime = appConciseUsages.get(appConciseUsages.size() - 1).time;//first chronological start date time
            LocalDateTime referenceDateTime =
                    LocalDateTime.of(firstStartDateTime.toLocalDate(), LocalTime.of(0, 0));

            for (int i = appConciseUsages.size() - 1; i > 0; i--) {
                Long durationInSeconds = appConciseUsages.get(i).getDurationInSeconds();

                LocalDateTime appUsageStartDateTime = appConciseUsages.get(i).time;

                LocalDateTime sessionStartDateTime =
                        referenceDateTime.toLocalDate().atTime(COPY_SESSION_START_TIME);
                LocalDateTime sessionEndDateTime =
                        referenceDateTime.toLocalDate().plusDays(1).atTime(COPY_SESSION_END_TIME);
                boolean isInCopySession = appUsageStartDateTime.isAfter(sessionStartDateTime)
                        && appUsageStartDateTime.isBefore(sessionEndDateTime);
                if (isInCopySession
                        && durationInSeconds > CONCISE_MIN_TIME_IN_SECONDS
                ) {
                    copyText.add(Utils.getAppModelTimeText(appConciseUsages, i));
                }
            }

            runOnUiThread(() -> {
                ClipData clipData = ClipData.newPlainText("label", String.join(" ", copyText));
                ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clipData);
            });
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> recyclerView.scrollToPosition(0));
        updateData();
        btnConcise.callOnClick(); // click btn concise
    }

    private void updateData() {

        UsageEvents usageEvents = usageStatsManager.queryEvents(BEGIN_TIME_IN_MILLIS, System.currentTimeMillis());

        Map<String, ArrayList<AppEvent>> appNameToAppEventMap = new HashMap<>();
        appUsages.clear();
        appConciseUsages.clear();
        appEvents.clear();

        // retrieve all events to appEvents and appNameToAppEventMap
        while (usageEvents.hasNextEvent()) {
            UsageEvents.Event event = new UsageEvents.Event();
            usageEvents.getNextEvent(event);

            String packageName = event.getPackageName();
            String eventType = EVENT_TYPE_MAP.get(event.getEventType());
            if (eventType == null || packageName == null)
                continue;

            AppEvent appEvent = new AppEvent();
            appEvent.eventType = eventType;
            appEvent.time = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTimeStamp()), ZoneId.systemDefault());

            try {// get app name
                PackageManager packageManager = getPackageManager();
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
                String appName = (String) packageManager.getApplicationLabel(appInfo);
                if (APP_NAME_EXCLUDED_LIST.contains(appName))
                    continue;
                appEvent.appName = appName;
            } catch (PackageManager.NameNotFoundException e) {
                Log.w("MyLog", String.format("App info is not found for %s", packageName));
                appEvent.appName = packageName;
            }

            try {// get app icon
                appEvent.appIcon = getPackageManager().getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w("MyLog", String.format("App Icon is not found for %s", packageName));
                appEvent.appIcon = AppCompatResources.getDrawable(this, android.R.drawable.sym_def_app_icon);
            }

            // add to eventsMap
            if (EVENT_TYPES_FOR_DURATION_LIST.contains(eventType)) {
                appNameToAppEventMap.computeIfAbsent(appEvent.appName, k -> new ArrayList<>()).add(appEvent);
            }
            // add to appEvents list
            appEvents.add(appEvent);
        }

        // calculate app usages
        for (Map.Entry<String, ArrayList<AppEvent>> entry : appNameToAppEventMap.entrySet()) {
            String appName = entry.getKey();
            ArrayList<AppEvent> events = entry.getValue();
            int y;

            for (int x = 0; x < events.size(); x++) {
                AppEvent eventX = events.get(x);

                if (isResumed(eventX)) {
                    y = x + 1;

                    while (y < events.size() && !isPausedOrStopped(events.get(y))) {
                        y++;
                    }

                    if (y < events.size()) {
                        AppEvent eventY = events.get(y);
                        long durationInSeconds = Duration.between(eventX.time, eventY.time).toMillis() / 1000;
                        if (durationInSeconds > 0) {
                            AppUsage appUsage = new AppUsage(appName, eventX.appIcon, eventX.time, durationInSeconds);
                            appUsage.setDurationInSeconds(durationInSeconds);
                            appUsages.add(appUsage);
                            x = y;
                        }
                    }
                }
            }
        }

        Collections.sort(appUsages);
        
        // calculate screen locked usage from app usages gaps
        for (int i = 0; i < appUsages.size() - 1; i++) {
            AppUsage currentAppUsage = appUsages.get(i);
            AppUsage nextAppUsage = appUsages.get(i + 1);

            LocalDateTime currentAppUsageEndTime = currentAppUsage.time.plusSeconds(currentAppUsage.getDurationInSeconds());
            Duration timeDiff = Duration.between(currentAppUsageEndTime, nextAppUsage.time);

            if (timeDiff.getSeconds() > 1) {
                AppUsage sreenLockedAppUsage = new AppUsage(
                        "Screen Locked",
                        AppCompatResources.getDrawable(this, android.R.drawable.sym_def_app_icon),
                        currentAppUsageEndTime, timeDiff.getSeconds());
                appUsages.add(i + 1, sreenLockedAppUsage);
                if (sreenLockedAppUsage.getDurationInSeconds() >= CONCISE_MIN_TIME_IN_SECONDS) {
                    //add long screen locked time to concise list
                    appConciseUsages.add(sreenLockedAppUsage);
                    appConciseUsages.add(nextAppUsage);
                }
                i++;
            }
        }

        Collections.reverse(appEvents);
        Collections.reverse(appUsages);
        Collections.reverse(appConciseUsages);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateAdapter(ArrayList<? extends AppModel> appModels) {
        listAdapter.setData(appModels);
        listAdapter.notifyDataSetChanged();
    }

    private boolean hasUsageAccessPermission() {
        // Check if permission is granted
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(),
                getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void highlightButton(Button button) {
        int btnPrimaryColor = getColor(R.color.md_theme_light_primary);
        int btnOnColor = getColor(R.color.md_theme_light_onPrimary);

        for (Button b : Arrays.asList(btnAll, btnConcise, btnRaw)) {
            b.setBackgroundColor(b.equals(button) ? btnPrimaryColor : btnOnColor);
            b.setTextColor(b.equals(button) ? btnOnColor : btnPrimaryColor);
        }
    }

    private boolean isResumed(AppEvent event) {
        return event.eventType.equals("Activity Resumed");
    }

    private boolean isPausedOrStopped(AppEvent event) {
        return event.eventType.equals("Activity Paused") || event.eventType.equals("Activity Stopped");
    }
}
