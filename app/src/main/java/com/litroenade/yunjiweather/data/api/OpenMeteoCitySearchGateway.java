package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.api.model.OpenMeteoGeocodingResponse;
import com.litroenade.yunjiweather.data.entity.CityEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Response;

public final class OpenMeteoCitySearchGateway {

    private static final String LOCATION_ID_PREFIX = "openmeteo:";

    private final OpenMeteoApiService geocodingService;

    public OpenMeteoCitySearchGateway(OpenMeteoApiService geocodingService) {
        this.geocodingService = geocodingService;
    }

    public CityEntity searchCity(String keyword, boolean isDefault, int sortOrder, long nowTime) throws IOException {
        List<CityEntity> cities = searchCities(keyword, isDefault, sortOrder, nowTime);
        if (cities.isEmpty()) {
            throw new IOException("未找到该城市，请检查城市名称。");
        }
        return cities.get(0);
    }

    public List<CityEntity> searchCities(String keyword, boolean isDefault, int sortOrder, long nowTime) throws IOException {
        Response<OpenMeteoGeocodingResponse> response = geocodingService.searchCity(
                requireText(keyword, "keyword"),
                8,
                "zh",
                "json",
                null
        ).execute();
        OpenMeteoGeocodingResponse body = response.body();
        if (!response.isSuccessful() || body == null) {
            throw new IOException("Open-Meteo 城市搜索接口请求失败");
        }
        if (body.results == null || body.results.isEmpty()) {
            throw new IOException("未找到该城市，请检查城市名称。");
        }
        List<CityEntity> cities = new ArrayList<>();
        for (int index = 0; index < body.results.size(); index++) {
            OpenMeteoGeocodingResponse.Location location = requireNonNull(body.results.get(index), "results[" + index + "]");
            Integer id = requireNonNull(location.id, "results[" + index + "].id");
            String name = requireText(location.name, "results[" + index + "].name");
            String country = requireText(location.country, "results[" + index + "].country");
            String province = textOrFallback(location.admin1, country);
            cities.add(new CityEntity(
                    name,
                    LOCATION_ID_PREFIX + id,
                    province,
                    country,
                    requireDouble(location.latitude, "results[" + index + "].latitude"),
                    requireDouble(location.longitude, "results[" + index + "].longitude"),
                    isDefault && index == 0,
                    sortOrder + index,
                    nowTime,
                    nowTime
            ));
        }
        return cities;
    }

    private static <T> T requireNonNull(T value, String fieldName) throws IOException {
        try {
            return Objects.requireNonNull(value, fieldName);
        } catch (NullPointerException exception) {
            throw new IOException("Open-Meteo 城市搜索接口缺少字段：" + fieldName, exception);
        }
    }

    private static String requireText(String value, String fieldName) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            throw new IOException("Open-Meteo 城市搜索接口缺少字段：" + fieldName);
        }
        return value.trim();
    }

    private static String textOrFallback(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private static double requireDouble(Double value, String fieldName) throws IOException {
        if (value == null) {
            throw new IOException("Open-Meteo 城市搜索接口缺少字段：" + fieldName);
        }
        return value;
    }
}
