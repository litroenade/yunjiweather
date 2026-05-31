package com.litroenade.yunjiweather.data.model;

public final class CustomThemeRule {

    public static final String LIGHT_ANY = "any";
    public static final String LIGHT_DAY = "day";
    public static final String LIGHT_NIGHT = "night";
    public static final int NO_TIME = -1;

    private final String assetId;
    private final String weatherKey;
    private final String lightMode;
    private final int startMinute;
    private final int endMinute;
    private final int priority;

    public CustomThemeRule(
            String assetId,
            String weatherKey,
            String lightMode,
            int startMinute,
            int endMinute,
            int priority
    ) {
        this.assetId = normalizeAssetId(assetId);
        this.weatherKey = CustomThemeWeatherKey.normalize(weatherKey);
        this.lightMode = normalizeLightMode(lightMode);
        this.startMinute = normalizeTimeBoundary(startMinute);
        this.endMinute = normalizeTimeBoundary(endMinute);
        this.priority = priority;
    }

    public static CustomThemeRule fallback(String assetId) {
        return new CustomThemeRule(
                assetId,
                CustomThemeWeatherKey.FALLBACK,
                LIGHT_ANY,
                NO_TIME,
                NO_TIME,
                0
        );
    }

    public String getAssetId() {
        return assetId;
    }

    public String getWeatherKey() {
        return weatherKey;
    }

    public String getLightMode() {
        return lightMode;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public int getPriority() {
        return priority;
    }

    public boolean hasTimeWindow() {
        return startMinute >= 0 && endMinute >= 0;
    }

    public boolean isFallbackRule() {
        return CustomThemeWeatherKey.FALLBACK.equals(weatherKey)
                && LIGHT_ANY.equals(lightMode)
                && !hasTimeWindow();
    }

    static String normalizeLightMode(String lightMode) {
        String normalized = lightMode == null ? "" : lightMode.trim().toLowerCase();
        if (LIGHT_DAY.equals(normalized) || LIGHT_NIGHT.equals(normalized)) {
            return normalized;
        }
        return LIGHT_ANY;
    }

    private static String normalizeAssetId(String assetId) {
        String normalized = assetId == null ? "" : assetId.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Custom theme rule asset id must not be blank");
        }
        return normalized;
    }

    private static int normalizeTimeBoundary(int minute) {
        if (minute < 0) {
            return NO_TIME;
        }
        int normalized = minute % (24 * 60);
        return normalized < 0 ? normalized + 24 * 60 : normalized;
    }
}
