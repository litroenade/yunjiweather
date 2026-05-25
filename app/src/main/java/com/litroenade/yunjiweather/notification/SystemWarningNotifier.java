package com.litroenade.yunjiweather.notification;

import android.content.Context;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

public final class SystemWarningNotifier implements WarningNotifier {

    private final Context context;

    public SystemWarningNotifier(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public boolean notify(WarningEntity warning) {
        return NotificationHelper.showWarningNotification(context, warning);
    }
}
