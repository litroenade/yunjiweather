package com.litroenade.yunjiweather.data.model;

import java.util.Objects;

public final class WeatherDailyData {

    private final String dateText;
    private final String tempMax;
    private final String tempMin;
    private final String condition;
    private final String iconCode;

    public WeatherDailyData(String dateText, String tempMax, String tempMin, String condition, String iconCode) {
        this.dateText = requireText(dateText, "dateText");
        this.tempMax = requireText(tempMax, "tempMax");
        this.tempMin = requireText(tempMin, "tempMin");
        this.condition = requireText(condition, "condition");
        this.iconCode = requireText(iconCode, "iconCode");
    }

    private static String requireText(String value, String fieldName) {
        String text = Objects.requireNonNull(value, fieldName);
        if (text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return text;
    }

    public String getDateText() {
        return dateText;
    }

    public String getTempMax() {
        return tempMax;
    }

    public String getTempMin() {
        return tempMin;
    }

    public String getCondition() {
        return condition;
    }

    public String getIconCode() {
        return iconCode;
    }
}
