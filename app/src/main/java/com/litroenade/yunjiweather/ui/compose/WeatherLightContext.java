package com.litroenade.yunjiweather.ui.compose;

import java.util.Calendar;
import java.util.Locale;

public final class WeatherLightContext {

    public enum Phase {
        DAWN,
        DAY,
        DUSK,
        NIGHT
    }

    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final int DAWN_BEFORE_SUNRISE_MINUTES = 45;
    private static final int DAWN_AFTER_SUNRISE_MINUTES = 70;
    private static final int DUSK_BEFORE_SUNSET_MINUTES = 75;
    private static final int DUSK_AFTER_SUNSET_MINUTES = 45;

    private final Phase phase;
    private final float dayProgress;
    private final float warmth;
    private final float exposure;
    private final boolean night;

    private WeatherLightContext(
            Phase phase,
            float dayProgress,
            float warmth,
            float exposure,
            boolean night
    ) {
        this.phase = phase;
        this.dayProgress = clamp01(dayProgress);
        this.warmth = clamp01(warmth);
        this.exposure = clamp01(exposure);
        this.night = night;
    }

    public static WeatherLightContext now(String sunrise, String sunset) {
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        int minuteOfDay = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        return fromMinuteOfDay(sunrise, sunset, minuteOfDay);
    }

    public static WeatherLightContext fromMinuteOfDay(String sunrise, String sunset, int currentMinute) {
        int normalizedMinute = normalizeMinute(currentMinute);
        Integer sunriseMinute = parseMinuteOfDay(sunrise);
        Integer sunsetMinute = parseMinuteOfDay(sunset);
        if (sunriseMinute == null || sunsetMinute == null || sunsetMinute <= sunriseMinute) {
            return fallbackFromMinute(normalizedMinute);
        }

        int dawnStart = Math.max(0, sunriseMinute - DAWN_BEFORE_SUNRISE_MINUTES);
        int dawnEnd = Math.min(MINUTES_PER_DAY - 1, sunriseMinute + DAWN_AFTER_SUNRISE_MINUTES);
        int duskStart = Math.max(0, sunsetMinute - DUSK_BEFORE_SUNSET_MINUTES);
        int duskEnd = Math.min(MINUTES_PER_DAY - 1, sunsetMinute + DUSK_AFTER_SUNSET_MINUTES);
        float dayProgress = (normalizedMinute - sunriseMinute) / (float) (sunsetMinute - sunriseMinute);
        dayProgress = clamp01(dayProgress);

        if (normalizedMinute < dawnStart || normalizedMinute > duskEnd) {
            return new WeatherLightContext(Phase.NIGHT, dayProgress, 0.12f, 0.18f, true);
        }
        if (normalizedMinute <= dawnEnd) {
            float dawnProgress = (normalizedMinute - dawnStart) / (float) Math.max(1, dawnEnd - dawnStart);
            return new WeatherLightContext(Phase.DAWN, dayProgress, 0.76f + 0.18f * dawnProgress, 0.34f + 0.34f * dawnProgress, false);
        }
        if (normalizedMinute < duskStart) {
            float noonCurve = (float) Math.sin(Math.PI * dayProgress);
            return new WeatherLightContext(Phase.DAY, dayProgress, 0.22f + 0.24f * noonCurve, 0.72f + 0.24f * noonCurve, false);
        }
        float duskProgress = (normalizedMinute - duskStart) / (float) Math.max(1, duskEnd - duskStart);
        return new WeatherLightContext(Phase.DUSK, dayProgress, 0.92f - 0.12f * duskProgress, 0.60f - 0.30f * duskProgress, false);
    }

    public Phase getPhase() {
        return phase;
    }

    public float getDayProgress() {
        return dayProgress;
    }

    public float getWarmth() {
        return warmth;
    }

    public float getExposure() {
        return exposure;
    }

    public boolean isNight() {
        return night;
    }

    private static WeatherLightContext fallbackFromMinute(int minute) {
        if (minute >= 5 * 60 && minute < 7 * 60) {
            return new WeatherLightContext(Phase.DAWN, 0.02f, 0.82f, 0.46f, false);
        }
        if (minute >= 7 * 60 && minute < 17 * 60) {
            float progress = (minute - 7 * 60) / (float) (10 * 60);
            float noonCurve = (float) Math.sin(Math.PI * progress);
            return new WeatherLightContext(Phase.DAY, progress, 0.22f + 0.24f * noonCurve, 0.72f + 0.24f * noonCurve, false);
        }
        if (minute >= 17 * 60 && minute < 19 * 60) {
            return new WeatherLightContext(Phase.DUSK, 0.98f, 0.82f, 0.42f, false);
        }
        return new WeatherLightContext(Phase.NIGHT, 0f, 0.12f, 0.18f, true);
    }

    private static Integer parseMinuteOfDay(String value) {
        if (value == null) {
            return null;
        }
        String[] parts = value.trim().split(":");
        if (parts.length != 2) {
            return null;
        }
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return null;
            }
            return hour * 60 + minute;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static int normalizeMinute(int minute) {
        int normalized = minute % MINUTES_PER_DAY;
        if (normalized < 0) {
            normalized += MINUTES_PER_DAY;
        }
        return normalized;
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
