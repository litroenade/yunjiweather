package com.litroenade.yunjiweather.worker;

public final class WorkerScopeUtils {

    private static final String WEATHER_ALERT_WORK_NAME = "weather_alert_check";
    private static final String DAILY_WEATHER_WORK_NAME = "daily_weather_reminder";
    private static final String WEATHER_WIDGET_REFRESH_WORK_NAME = "weather_widget_refresh";
    private static final String WEATHER_WIDGET_IMMEDIATE_REFRESH_WORK_NAME = "weather_widget_immediate_refresh";

    private WorkerScopeUtils() {
    }

    public static String weatherAlertWorkName() {
        return WEATHER_ALERT_WORK_NAME;
    }

    public static String dailyWeatherWorkName() {
        return DAILY_WEATHER_WORK_NAME;
    }

    public static String weatherWidgetRefreshWorkName() {
        return WEATHER_WIDGET_REFRESH_WORK_NAME;
    }

    public static String weatherWidgetImmediateRefreshWorkName() {
        return WEATHER_WIDGET_IMMEDIATE_REFRESH_WORK_NAME;
    }
}
