package com.litroenade.yunjiweather.utils;

public final class ThemeModeUtils {

    private ThemeModeUtils() {
    }

    public static int resolveNightMode(boolean darkModeEnabled, int nightModeYes, int nightModeNo) {
        return darkModeEnabled ? nightModeYes : nightModeNo;
    }

    public static boolean shouldApplyNightMode(int currentMode, int targetMode) {
        return currentMode != targetMode;
    }
}
