package com.litroenade.yunjiweather.notification;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.litroenade.yunjiweather.MainActivity;
import com.litroenade.yunjiweather.R;
import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.utils.PermissionUtils;

public final class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static final String WARNING_CHANNEL_ID = "weather_warning";
    private static final String WARNING_CHANNEL_NAME = "天气预警";
    private static final String DAILY_CHANNEL_ID = "daily_weather";
    private static final String DAILY_CHANNEL_NAME = "每日天气提醒";

    private NotificationHelper() {
    }

    public static void createWarningChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                WARNING_CHANNEL_ID,
                WARNING_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("用于推送默认城市的天气预警信息");
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public static void createDailyReminderChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                DAILY_CHANNEL_ID,
                DAILY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("用于推送默认城市的每日天气提醒");
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    @SuppressLint("MissingPermission")
    public static boolean showWarningNotification(Context context, WarningEntity warning) {
        createWarningChannel(context);
        if (!PermissionUtils.hasNotificationPermission(context)) {
            return false;
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                warning.warningId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, WARNING_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(warning.title)
                .setContentText(warning.content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(warning.content))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        try {
            NotificationManagerCompat.from(context).notify(warning.warningId.hashCode(), builder.build());
            return true;
        } catch (SecurityException exception) {
            Log.w(TAG, "系统拒绝发送天气预警通知", exception);
            return false;
        }
    }

    @SuppressLint("MissingPermission")
    public static boolean showDailyWeatherNotification(Context context, String title, String content) {
        createDailyReminderChannel(context);
        if (!PermissionUtils.hasNotificationPermission(context)) {
            return false;
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                DAILY_CHANNEL_ID.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, DAILY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        try {
            NotificationManagerCompat.from(context).notify(DAILY_CHANNEL_ID.hashCode(), builder.build());
            return true;
        } catch (SecurityException exception) {
            Log.w(TAG, "系统拒绝发送每日天气提醒", exception);
            return false;
        }
    }
}
