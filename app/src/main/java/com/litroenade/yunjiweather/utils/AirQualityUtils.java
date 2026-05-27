package com.litroenade.yunjiweather.utils;

public final class AirQualityUtils {

    private AirQualityUtils() {
    }

    public static String toUsAqiCategory(int aqi) {
        requireNonNegativeAqi(aqi);
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

    public static int parseUsAqiDisplay(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("aqi must not be empty");
        }
        try {
            int aqi = (int) Math.round(Double.parseDouble(value.trim()));
            requireNonNegativeAqi(aqi);
            return aqi;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("aqi format is invalid", exception);
        }
    }

    public static String activityAdviceForUsAqi(int aqi) {
        requireNonNegativeAqi(aqi);
        if (aqi <= 50) {
            return "空气很好，适合户外活动。";
        }
        if (aqi <= 100) {
            return "空气可接受，户外活动留意体感。";
        }
        if (aqi <= 150) {
            return "敏感人群减少高强度户外运动。";
        }
        if (aqi <= 200) {
            return "建议减少长时间户外活动。";
        }
        if (aqi <= 300) {
            return "建议避免长时间户外活动。";
        }
        return "尽量留在室内并关闭门窗。";
    }

    public static String sensitiveGroupAdviceForUsAqi(int aqi) {
        requireNonNegativeAqi(aqi);
        if (aqi <= 50) {
            return "敏感人群可正常安排日常活动。";
        }
        if (aqi <= 100) {
            return "敏感人群留意咽喉和呼吸道感受。";
        }
        if (aqi <= 150) {
            return "敏感人群建议减少户外运动。";
        }
        return "老人、儿童和心肺敏感人群建议减少户外停留。";
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

    private static void requireNonNegativeAqi(int aqi) {
        if (aqi < 0) {
            throw new IllegalArgumentException("aqi must not be negative");
        }
    }
}
