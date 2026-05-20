package com.litroenade.yunjiweather.worker;

import com.litroenade.yunjiweather.auth.AuthSessionManager;

public final class WorkerScopeUtils {

    public static final String KEY_OWNER_USER_ID = "owner_user_id";

    private static final String WEATHER_ALERT_WORK_PREFIX = "weather_alert_check_user_";
    private static final String DAILY_WEATHER_WORK_PREFIX = "daily_weather_reminder_user_";

    private WorkerScopeUtils() {
    }

    public static String weatherAlertWorkName(long ownerUserId) {
        return WEATHER_ALERT_WORK_PREFIX + ownerUserId;
    }

    public static String dailyWeatherWorkName(long ownerUserId) {
        return DAILY_WEATHER_WORK_PREFIX + ownerUserId;
    }

    public static boolean shouldRunForCurrentUser(long scheduledOwnerUserId, AuthSessionManager sessionManager) {
        if (scheduledOwnerUserId <= 0L || !sessionManager.isLoggedIn()) {
            return false;
        }
        return sessionManager.requireUserId() == scheduledOwnerUserId;
    }
}
