package com.litroenade.yunjiweather.utils;

public final class WindScaleUtils {

    private static final double[] BEAUFORT_LIMITS_KMH = {
            1.0d, 6.0d, 12.0d, 20.0d, 29.0d, 39.0d,
            50.0d, 62.0d, 75.0d, 89.0d, 103.0d, 118.0d
    };

    private WindScaleUtils() {
    }

    public static int toWindScale(double speedKmh) {
        if (speedKmh < 0.0d) {
            throw new IllegalArgumentException("speedKmh must not be negative");
        }
        for (int i = 0; i < BEAUFORT_LIMITS_KMH.length; i++) {
            if (speedKmh < BEAUFORT_LIMITS_KMH[i]) {
                return i;
            }
        }
        return 12;
    }

    public static int parseDisplayScale(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("windScale must not be empty");
        }
        String text = value.trim();
        String[] parts = text.split("-");
        try {
            int scale = Integer.parseInt(parts[0].trim());
            if (scale < 0) {
                throw new IllegalArgumentException("windScale must not be negative");
            }
            return scale;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("windScale format is invalid", exception);
        }
    }

    public static String toWindDirectionText(double degrees) {
        if (degrees < 0.0d || degrees > 360.0d) {
            throw new IllegalArgumentException("degrees must be between 0 and 360");
        }
        if (degrees >= 337.5d || degrees < 22.5d) {
            return "北风";
        }
        if (degrees < 67.5d) {
            return "东北风";
        }
        if (degrees < 112.5d) {
            return "东风";
        }
        if (degrees < 157.5d) {
            return "东南风";
        }
        if (degrees < 202.5d) {
            return "南风";
        }
        if (degrees < 247.5d) {
            return "西南风";
        }
        if (degrees < 292.5d) {
            return "西风";
        }
        return "西北风";
    }
}
