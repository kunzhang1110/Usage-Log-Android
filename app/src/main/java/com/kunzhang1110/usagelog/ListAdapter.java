package com.kunzhang1110.usagelog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kunzhang1110.usagelog.models.AppEvent;
import com.kunzhang1110.usagelog.models.AppModel;
import com.kunzhang1110.usagelog.models.AppUsage;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private final DateTimeFormatter dateTimeFormatter;
    private List<? extends AppModel> data = new ArrayList<>();

    private final Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView appName;
        private final ImageView appIcon;
        private final TextView time;
        private final TextView subText;

        protected ViewHolder(View view) {
            super(view);
            appName = view.findViewById(R.id.textview_row_app_name);
            appIcon = view.findViewById(R.id.imageview_row_app_icon);
            time = view.findViewById(R.id.textview_row_time);
            subText = view.findViewById(R.id.textview_row_additional);

            view.setOnLongClickListener(v -> {
                ClipboardManager clipboardManager = (ClipboardManager) context
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                int index = getAdapterPosition();
                if (index < 1)
                    return true;

                ClipData clipData = ClipData.newPlainText("label", Utils.getAppModelTimeText(data, index));
                // Set the ClipData to the clipboard
                clipboardManager.setPrimaryClip(clipData);
                return true;
            });
        }

        public TextView getAppName() {
            return appName;
        }

        public ImageView getAppIcon() {
            return appIcon;
        }

        public TextView getTime() {
            return time;
        }

        public TextView getSubText() {
            return subText;
        }
    }

    public ListAdapter(Context context, DateTimeFormatter dateTimeFormatter) {
        this.context = context;
        this.dateTimeFormatter = dateTimeFormatter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {

        AppModel appModel = data.get(position);
        if (appModel instanceof AppUsage) {
            Long durationInSeconds = ((AppUsage) appModel).durationInSeconds;

            if (durationInSeconds != null) {
                int textColor = (durationInSeconds >= Constants.CONCISE_MIN_TIME_IN_SECONDS) ? Color.RED : Color.BLACK;
                viewHolder.getAppName().setTextColor(textColor);
                viewHolder.getSubText().setTextColor(textColor);
            }
            viewHolder.getSubText().setText(((AppUsage) appModel).durationInText);
        }
        if (appModel instanceof AppEvent) {
            viewHolder.getSubText().setText(((AppEvent) appModel).eventType);
            viewHolder.getAppName().setTextColor(Color.BLACK);
            viewHolder.getSubText().setTextColor(Color.BLACK);
        }

        viewHolder.getAppName().setText(appModel.appName);
        viewHolder.getTime().setText(dateTimeFormatter.format(appModel.time));

        viewHolder.getAppIcon().setImageDrawable(appModel.appIcon);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(ArrayList<? extends AppModel> appModels) {
        this.data = appModels;
    }
}
