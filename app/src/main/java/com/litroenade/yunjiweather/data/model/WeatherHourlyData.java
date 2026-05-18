package com.litroenade.yunjiweather.data.model;

import java.util.Objects;

public final class WeatherHourlyData {

    private final String timeText;
    private final String temperature;
    private final String condition;
    private final String iconCode;

    public WeatherHourlyData(String timeText, String temperature, String condition, String iconCode) {
        this.timeText = requireText(timeText, "timeText");
        this.temperature = requireText(temperature, "temperature");
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

    public String getTimeText() {
        return timeText;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getCondition() {
        return condition;
    }

    public String getIconCode() {
        return iconCode;
    }
}
