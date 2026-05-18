package com.litroenade.yunjiweather.utils;

public final class AirQualityUtils {

    private AirQualityUtils() {
    }

    public static String toUsAqiCategory(int aqi) {
        if (aqi < 0) {
            throw new IllegalArgumentException("aqi must not be negative");
        }
        if (aqi <= 50) {
            return "优";
        }
        if (aqi <= 100) {
            return "良";
        }
        if (aqi <= 150) {
            return "对敏感人群不健康";
        }
        if (aqi <= 200) {
            return "不健康";
        }
        if (aqi <= 300) {
            return "非常不健康";
        }
        return "危险";
    }

    public static String findPrimaryPollutant(
            Double pm25,
            Double pm10,
            Double nitrogenDioxide,
            Double ozone,
            Double sulphurDioxide,
            Double carbonMonoxide
    ) {
        String[] names = {"PM2.5", "PM10", "NO2", "O3", "SO2", "CO"};
        Double[] values = {pm25, pm10, nitrogenDioxide, ozone, sulphurDioxide, carbonMonoxide};
        String bestName = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.length; i++) {
            Double value = values[i];
            if (value != null && value > bestValue) {
                bestValue = value;
                bestName = names[i];
            }
        }
        if (bestName == null) {
            throw new IllegalArgumentException("at least one pollutant index is required");
        }
        return bestName;
    }
}
