package com.litroenade.yunjiweather.ui.compose;

import com.litroenade.yunjiweather.utils.WeatherIconUtils;

public final class WeatherSceneSpec {

    public enum Precipitation {
        NONE,
        RAIN,
        SNOW
    }

    private final WeatherIconUtils.WeatherCategory category;
    private final Precipitation precipitation;
    private final int topColor;
    private final int middleColor;
    private final int bottomColor;
    private final float cloudOpacity;
    private final float hazeOpacity;
    private final float atmosphereAlpha;
    private final int particleCount;
    private final boolean celestialGlow;
    private final boolean lightForeground;

    private WeatherSceneSpec(
            WeatherIconUtils.WeatherCategory category,
            Precipitation precipitation,
            int topColor,
            int middleColor,
            int bottomColor,
            float cloudOpacity,
            float hazeOpacity,
            float atmosphereAlpha,
            int particleCount,
            boolean celestialGlow,
            boolean lightForeground
    ) {
        this.category = category;
        this.precipitation = precipitation;
        this.topColor = topColor;
        this.middleColor = middleColor;
        this.bottomColor = bottomColor;
        this.cloudOpacity = cloudOpacity;
        this.hazeOpacity = hazeOpacity;
        this.atmosphereAlpha = atmosphereAlpha;
        this.particleCount = particleCount;
        this.celestialGlow = celestialGlow;
        this.lightForeground = lightForeground;
    }

    public static WeatherSceneSpec fromIconCode(String iconCode) {
        WeatherIconUtils.WeatherCategory category = WeatherIconUtils.getWeatherCategory(iconCode);
        switch (category) {
            case SUNNY:
                return new WeatherSceneSpec(
                        category,
                        Precipitation.NONE,
                        0xFF5EA7D9,
                        0xFF94C5D6,
                        0xFFCAD6D4,
                        0.18f,
                        0.20f,
                        0.22f,
                        0,
                        true,
                        false
                );
            case NIGHT:
                return new WeatherSceneSpec(
                        category,
                        Precipitation.NONE,
                        0xFF14243A,
                        0xFF344E69,
                        0xFFE6E1D5,
                        0.22f,
                        0.28f,
                        0.24f,
                        0,
                        true,
                        true
                );
            case RAIN:
                return new WeatherSceneSpec(
                        category,
                        Precipitation.RAIN,
                        0xFF455C6C,
                        0xFF617A87,
                        0xFF8C999C,
                        0.68f,
                        0.48f,
                        0.32f,
                        52,
                        false,
                        true
                );
            case SNOW:
                return new WeatherSceneSpec(
                        category,
                        Precipitation.SNOW,
                        0xFFAECBE0,
                        0xFFC8D8E0,
                        0xFFD8DDD8,
                        0.42f,
                        0.30f,
                        0.30f,
                        38,
                        false,
                        false
                );
            case CLOUDY:
            default:
                return new WeatherSceneSpec(
                        category,
                        Precipitation.NONE,
                        0xFF6E8292,
                        0xFF8EA2AA,
                        0xFFB3B8B5,
                        0.40f,
                        0.34f,
                        0.24f,
                        0,
                        false,
                        true
                );
        }
    }

    public WeatherIconUtils.WeatherCategory getCategory() {
        return category;
    }

    public Precipitation getPrecipitation() {
        return precipitation;
    }

    public int getTopColor() {
        return topColor;
    }

    public int getMiddleColor() {
        return middleColor;
    }

    public int getBottomColor() {
        return bottomColor;
    }

    public float getCloudOpacity() {
        return cloudOpacity;
    }

    public float getHazeOpacity() {
        return hazeOpacity;
    }

    public float getAtmosphereAlpha() {
        return atmosphereAlpha;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public boolean hasCelestialGlow() {
        return celestialGlow;
    }

    public boolean usesLightForeground() {
        return lightForeground;
    }
}
