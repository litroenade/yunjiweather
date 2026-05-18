package com.litroenade.yunjiweather.data.model;

import com.litroenade.yunjiweather.common.UiState;

import java.util.Objects;

public final class CityWeatherSummary {

    private static final String UNAVAILABLE_TEXT = "天气摘要暂不可用";

    private final String locationId;
    private final String temperature;
    private final String condition;
    private final String tempMax;
    private final String tempMin;
    private final long updateTime;
    private final boolean fromCache;
    private final String errorMessage;

    public CityWeatherSummary(
            String locationId,
            String temperature,
            String condition,
            String tempMax,
            String tempMin,
            long updateTime,
            boolean fromCache,
            String errorMessage
    ) {
        this.locationId = requireText(locationId, "locationId");
        this.temperature = requireNullableText(temperature, "temperature");
        this.condition = requireNullableText(condition, "condition");
        this.tempMax = requireNullableText(tempMax, "tempMax");
        this.tempMin = requireNullableText(tempMin, "tempMin");
        this.updateTime = updateTime;
        this.fromCache = fromCache;
        this.errorMessage = requireNullableText(errorMessage, "errorMessage");
    }

    public static CityWeatherSummary fromWeatherState(String locationId, UiState<HomeWeatherData> state) {
        if (state == null || state.getData() == null) {
            return unavailable(locationId);
        }
        HomeWeatherData data = state.getData();
        boolean cache = state.getStatus() == UiState.Status.CACHE;
        long updateTime = cache ? state.getUpdateTime() : data.getUpdateTime();
        return new CityWeatherSummary(
                locationId,
                data.getTemperature(),
                data.getCondition(),
                data.getTempMax(),
                data.getTempMin(),
                updateTime,
                cache,
                ""
        );
    }

    public static CityWeatherSummary unavailable(String locationId) {
        return new CityWeatherSummary(locationId, "", "", "", "", 0L, false, UNAVAILABLE_TEXT);
    }

    private static String requireText(String value, String fieldName) {
        String text = Objects.requireNonNull(value, fieldName);
        if (text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return text;
    }

    private static String requireNullableText(String value, String fieldName) {
        String text = Objects.requireNonNull(value, fieldName);
        if (!text.isEmpty() && text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return text;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getCondition() {
        return condition;
    }

    public String getTempMax() {
        return tempMax;
    }

    public String getTempMin() {
        return tempMin;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
