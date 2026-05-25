package com.litroenade.yunjiweather.ui.alert;

import androidx.annotation.ColorRes;

import com.litroenade.yunjiweather.R;

import java.util.Locale;

public final class WarningStyleUtils {

    private static final String BLUE_HEX = "#2F6DAE";
    private static final String YELLOW_HEX = "#9A6A00";
    private static final String ORANGE_HEX = "#B85C1E";
    private static final String RED_HEX = "#B42318";
    private static final String NEUTRAL_HEX = "#6B7280";

    private WarningStyleUtils() {
    }

    @ColorRes
    public static int resolveColorRes(String level) {
        return resolveLevel(level).colorRes;
    }

    public static String resolveColorHex(String level) {
        return resolveLevel(level).colorHex;
    }

    private static WarningLevel resolveLevel(String level) {
        if (level == null) {
            return WarningLevel.NEUTRAL;
        }
        String normalizedLevel = level.toLowerCase(Locale.ROOT);
        if (normalizedLevel.contains("red") || normalizedLevel.contains("红")) {
            return WarningLevel.RED;
        }
        if (normalizedLevel.contains("orange") || normalizedLevel.contains("橙")) {
            return WarningLevel.ORANGE;
        }
        if (normalizedLevel.contains("yellow") || normalizedLevel.contains("黄")) {
            return WarningLevel.YELLOW;
        }
        if (normalizedLevel.contains("blue") || normalizedLevel.contains("蓝")) {
            return WarningLevel.BLUE;
        }
        return WarningLevel.NEUTRAL;
    }

    private enum WarningLevel {
        BLUE(BLUE_HEX, R.color.warning_blue),
        YELLOW(YELLOW_HEX, R.color.warning_yellow),
        ORANGE(ORANGE_HEX, R.color.warning_orange),
        RED(RED_HEX, R.color.warning_red),
        NEUTRAL(NEUTRAL_HEX, R.color.warning_neutral);

        private final String colorHex;
        private final int colorRes;

        WarningLevel(String colorHex, @ColorRes int colorRes) {
            this.colorHex = colorHex;
            this.colorRes = colorRes;
        }
    }
}
