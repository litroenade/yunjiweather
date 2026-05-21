package com.litroenade.yunjiweather.ui.alert;

import android.graphics.Color;

import java.util.Locale;

public final class WarningStyleUtils {

    private static final String BLUE = "#155EEF";
    private static final String YELLOW = "#B54708";
    private static final String ORANGE = "#C4320A";
    private static final String RED = "#B42318";
    private static final String NEUTRAL = "#667085";

    private WarningStyleUtils() {
    }

    public static int resolveColor(String level) {
        return Color.parseColor(resolveColorHex(level));
    }

    public static String resolveColorHex(String level) {
        if (level == null) {
            return NEUTRAL;
        }
        String normalizedLevel = level.toLowerCase(Locale.ROOT);
        if (normalizedLevel.contains("red") || normalizedLevel.contains("红")) {
            return RED;
        }
        if (normalizedLevel.contains("orange") || normalizedLevel.contains("橙")) {
            return ORANGE;
        }
        if (normalizedLevel.contains("yellow") || normalizedLevel.contains("黄")) {
            return YELLOW;
        }
        if (normalizedLevel.contains("blue") || normalizedLevel.contains("蓝")) {
            return BLUE;
        }
        return NEUTRAL;
    }
}
