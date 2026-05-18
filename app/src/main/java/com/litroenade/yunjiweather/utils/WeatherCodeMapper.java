package com.litroenade.yunjiweather.utils;

public final class WeatherCodeMapper {

    private WeatherCodeMapper() {
    }

    public static String toCondition(int code) {
        switch (code) {
            case 0:
                return "晴";
            case 1:
            case 2:
                return "多云";
            case 3:
                return "阴";
            case 45:
            case 48:
                return "雾";
            case 51:
            case 53:
            case 55:
                return "毛毛雨";
            case 56:
            case 57:
            case 66:
            case 67:
                return "冻雨";
            case 61:
            case 63:
            case 65:
                return "雨";
            case 71:
            case 73:
            case 75:
            case 77:
                return "雪";
            case 80:
            case 81:
            case 82:
                return "阵雨";
            case 85:
            case 86:
                return "阵雪";
            case 95:
            case 96:
            case 99:
                return "雷雨";
            default:
                throw new IllegalArgumentException("Unsupported weather code: " + code);
        }
    }

    public static String toIconCode(int code, boolean isDay) {
        switch (code) {
            case 0:
                return isDay ? "100" : "150";
            case 1:
            case 2:
                return "101";
            case 3:
                return "104";
            case 45:
            case 48:
                return "501";
            case 51:
            case 53:
            case 55:
            case 61:
            case 63:
            case 65:
            case 80:
            case 81:
            case 82:
                return "305";
            case 56:
            case 57:
            case 66:
            case 67:
                return "313";
            case 71:
            case 73:
            case 75:
            case 77:
            case 85:
            case 86:
                return "400";
            case 95:
            case 96:
            case 99:
                return "302";
            default:
                throw new IllegalArgumentException("Unsupported weather code: " + code);
        }
    }
}
