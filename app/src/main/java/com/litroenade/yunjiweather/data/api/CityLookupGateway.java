package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.api.model.QWeatherCityLookupResponse;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.utils.LocationQueryUtils;

import java.io.IOException;
import java.util.Locale;

import retrofit2.Response;

public final class CityLookupGateway {

    private static final String SUCCESS_CODE = "200";

    private final WeatherApiService qWeatherApiService;
    private final OpenMeteoCitySearchGateway openMeteoCitySearchGateway;

    public CityLookupGateway(
            WeatherApiService qWeatherApiService,
            OpenMeteoCitySearchGateway openMeteoCitySearchGateway
    ) {
        this.qWeatherApiService = qWeatherApiService;
        this.openMeteoCitySearchGateway = openMeteoCitySearchGateway;
    }

    public CityEntity searchCity(String keyword, boolean isDefault, int sortOrder, long nowTime) throws IOException {
        String normalized = requireText(keyword, "keyword").trim();
        CityEntity presetCity = createPresetCity(normalized, isDefault, nowTime);
        if (presetCity != null) {
            return presetCity;
        }
        if (qWeatherApiService == null) {
            if (openMeteoCitySearchGateway == null) {
                throw new IOException("城市搜索服务未配置");
            }
            return openMeteoCitySearchGateway.searchCity(normalized, isDefault, sortOrder, nowTime);
        }
        Response<QWeatherCityLookupResponse> response = qWeatherApiService.searchCity(
                normalized,
                "cn",
                1,
                "zh"
        ).execute();
        QWeatherCityLookupResponse body = response.body();
        if (!response.isSuccessful() || body == null || !SUCCESS_CODE.equals(body.code)) {
            throw new IOException("城市搜索接口请求失败，请检查网络或 API 配置。");
        }
        if (body.location == null || body.location.isEmpty()) {
            throw new IOException("未找到该城市，请检查城市名称。");
        }
        return mapQWeatherLocation(body.location.get(0), isDefault, sortOrder, nowTime, "城市搜索接口");
    }

    public CityEntity reverseLookup(double latitude, double longitude, long nowTime) throws IOException {
        if (qWeatherApiService == null) {
            return createCoordinateCity(latitude, longitude, nowTime);
        }
        String locationQuery = LocationQueryUtils.formatQWeatherLocationQuery(latitude, longitude);
        Response<QWeatherCityLookupResponse> response = qWeatherApiService.searchCity(
                locationQuery,
                "cn",
                1,
                "zh"
        ).execute();
        QWeatherCityLookupResponse body = response.body();
        if (!response.isSuccessful() || body == null || !SUCCESS_CODE.equals(body.code)) {
            throw new IOException("城市反查接口请求失败");
        }
        if (body.location == null || body.location.isEmpty()) {
            throw new IOException("未找到当前位置对应城市");
        }
        return mapQWeatherLocation(body.location.get(0), true, 0, nowTime, "城市反查接口");
    }

    static CityEntity mapQWeatherLocation(
            QWeatherCityLookupResponse.Location location,
            boolean isDefault,
            int sortOrder,
            long nowTime,
            String sourceName
    ) throws IOException {
        if (location == null) {
            throw new IOException(sourceName + "缺少字段：location");
        }
        return new CityEntity(
                requireQWeatherText(location.name, "location.name", sourceName),
                requireQWeatherText(location.id, "location.id", sourceName),
                requireQWeatherText(location.adm1, "location.adm1", sourceName),
                requireQWeatherText(location.country, "location.country", sourceName),
                parseCoordinate(location.lat, "location.lat", sourceName),
                parseCoordinate(location.lon, "location.lon", sourceName),
                isDefault,
                sortOrder,
                nowTime,
                nowTime
        );
    }

    private static CityEntity createPresetCity(String cityName, boolean isDefault, long nowTime) {
        if ("北京".equals(cityName)) {
            return new CityEntity("北京", "101010100", "北京", "中国", 39.9042, 116.4074, isDefault, 0, nowTime, nowTime);
        }
        if ("上海".equals(cityName)) {
            return new CityEntity("上海", "101020100", "上海", "中国", 31.2304, 121.4737, isDefault, 1, nowTime, nowTime);
        }
        if ("广州".equals(cityName)) {
            return new CityEntity("广州", "101280101", "广东", "中国", 23.1291, 113.2644, isDefault, 2, nowTime, nowTime);
        }
        if ("深圳".equals(cityName)) {
            return new CityEntity("深圳", "101280601", "广东", "中国", 22.5431, 114.0579, isDefault, 3, nowTime, nowTime);
        }
        return null;
    }

    private static CityEntity createCoordinateCity(double latitude, double longitude, long nowTime) {
        return new CityEntity(
                "当前位置",
                String.format(Locale.US, "openmeteo:%.4f,%.4f", latitude, longitude),
                "定位坐标",
                "GPS",
                latitude,
                longitude,
                true,
                0,
                nowTime,
                nowTime
        );
    }

    private static String requireText(String value, String fieldName) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            throw new IOException("城市搜索接口缺少字段：" + fieldName);
        }
        return value;
    }

    private static String requireQWeatherText(String value, String fieldName, String sourceName) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            throw new IOException(sourceName + "缺少字段：" + fieldName);
        }
        return value;
    }

    private static double parseCoordinate(String value, String fieldName, String sourceName) throws IOException {
        try {
            return Double.parseDouble(requireQWeatherText(value, fieldName, sourceName));
        } catch (NumberFormatException exception) {
            throw new IOException(sourceName + "坐标格式错误：" + fieldName, exception);
        }
    }
}
