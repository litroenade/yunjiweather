package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.api.model.OpenMeteoGeocodingResponse;
import com.litroenade.yunjiweather.data.entity.CityEntity;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Response;

public final class OpenMeteoCitySearchGateway {

    private static final String LOCATION_ID_PREFIX = "openmeteo:";

    private final OpenMeteoApiService geocodingService;

    public OpenMeteoCitySearchGateway(OpenMeteoApiService geocodingService) {
        this.geocodingService = geocodingService;
    }

    public CityEntity searchCity(String keyword, boolean isDefault, int sortOrder, long nowTime) throws IOException {
        Response<OpenMeteoGeocodingResponse> response = geocodingService.searchCity(
                requireText(keyword, "keyword"),
                1,
                "zh",
                "json",
                "CN"
        ).execute();
        OpenMeteoGeocodingResponse body = response.body();
        if (!response.isSuccessful() || body == null) {
            throw new IOException("Open-Meteo 城市搜索接口请求失败");
        }
        if (body.results == null || body.results.isEmpty()) {
            throw new IOException("未找到该城市，请检查城市名称。");
        }
        OpenMeteoGeocodingResponse.Location location = requireNonNull(body.results.get(0), "results[0]");
        Integer id = requireNonNull(location.id, "results[0].id");
        return new CityEntity(
                requireText(location.name, "results[0].name"),
                LOCATION_ID_PREFIX + id,
                requireText(location.admin1, "results[0].admin1"),
                requireText(location.country, "results[0].country"),
                requireDouble(location.latitude, "results[0].latitude"),
                requireDouble(location.longitude, "results[0].longitude"),
                isDefault,
                sortOrder,
                nowTime,
                nowTime
        );
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

    private static double requireDouble(Double value, String fieldName) throws IOException {
        if (value == null) {
            throw new IOException("Open-Meteo 城市搜索接口缺少字段：" + fieldName);
        }
        return value;
    }
}
