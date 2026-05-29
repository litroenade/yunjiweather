package com.litroenade.yunjiweather.widget;

public enum WeatherWidgetLayoutMode {
    AUTO,
    COMPACT,
    STANDARD,
    EXPANDED;

    public static WeatherWidgetLayoutMode fromSize(int minWidthDp, int minHeightDp) {
        if (minWidthDp <= 0 && minHeightDp <= 0) {
            return STANDARD;
        }
        if (minWidthDp >= 300 && minHeightDp >= 150) {
            return EXPANDED;
        }
        if (minWidthDp >= 220 && minHeightDp >= 108) {
            return STANDARD;
        }
        return COMPACT;
    }
}
