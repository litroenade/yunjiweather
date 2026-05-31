package com.litroenade.yunjiweather.widget;

public enum WeatherWidgetLayoutMode {
    AUTO,
    COMPACT,
    STANDARD,
    EXPANDED;

    public static WeatherWidgetLayoutMode fromSize(int minWidthDp, int minHeightDp) {
        return WidgetStyleSpec.modeForSize(minWidthDp, minHeightDp);
    }
}
