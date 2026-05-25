package com.litroenade.yunjiweather.notification;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

public interface WarningNotifier {
    boolean notify(WarningEntity warning);
}
