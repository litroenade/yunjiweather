package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.entity.CityEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class CityLookupGateway {

    private final OpenMeteoCitySearchGateway openMeteoCitySearchGateway;

    public CityLookupGateway(OpenMeteoCitySearchGateway openMeteoCitySearchGateway) {
        this.openMeteoCitySearchGateway = openMeteoCitySearchGateway;
    }

    public CityEntity searchCity(String keyword, boolean isDefault, int sortOrder, long nowTime) throws IOException {
        String normalized = requireText(keyword, "keyword").trim();
        CityEntity presetCity = createPresetCity(normalized, isDefault, nowTime);
        if (presetCity != null) {
            return presetCity;
        }
        if (openMeteoCitySearchGateway == null) {
            throw new IOException("Open-Meteo 城市搜索服务未配置");
        }
        return openMeteoCitySearchGateway.searchCity(normalized, isDefault, sortOrder, nowTime);
    }

    public List<CityEntity> searchCities(String keyword, int sortOrder, long nowTime) throws IOException {
        String normalized = requireText(keyword, "keyword").trim();
        CityEntity presetCity = createPresetCity(normalized, false, nowTime);
        if (presetCity != null) {
            presetCity.sortOrder = sortOrder;
            return Collections.singletonList(presetCity);
        }
        if (openMeteoCitySearchGateway == null) {
            throw new IOException("Open-Meteo 城市搜索服务未配置");
        }
        return openMeteoCitySearchGateway.searchCities(normalized, false, sortOrder, nowTime);
    }

    public CityEntity reverseLookup(double latitude, double longitude, long nowTime) {
        return createCoordinateCity(latitude, longitude, nowTime);
    }

    private static CityEntity createPresetCity(String cityName, boolean isDefault, long nowTime) {
        String normalizedCityName = normalizePresetCityName(cityName);
        switch (normalizedCityName) {
            case "北京":
            case "beijing":
            case "bj":
                return new CityEntity("北京", "openmeteo:1816670", "北京", "中国", 39.9042, 116.4074, isDefault, 0, nowTime, nowTime);
            case "上海":
            case "shanghai":
            case "sh":
                return new CityEntity("上海", "openmeteo:1796236", "上海", "中国", 31.2304, 121.4737, isDefault, 1, nowTime, nowTime);
            case "广州":
            case "guangzhou":
            case "gz":
                return new CityEntity("广州", "openmeteo:1809858", "广东", "中国", 23.1291, 113.2644, isDefault, 2, nowTime, nowTime);
            case "深圳":
            case "shenzhen":
            case "sz":
                return new CityEntity("深圳", "openmeteo:1795565", "广东", "中国", 22.5431, 114.0579, isDefault, 3, nowTime, nowTime);
        }
        return null;
    }

    private static String normalizePresetCityName(String cityName) {
        String normalized = cityName.trim();
        if (normalized.endsWith("市")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        String asciiKeyword = normalized
                .toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "");
        switch (asciiKeyword) {
            case "beijingshi":
                return "beijing";
            case "shanghaishi":
                return "shanghai";
            case "guangzhoushi":
                return "guangzhou";
            case "shenzhenshi":
                return "shenzhen";
            default:
                return asciiKeyword.matches("[a-z]+") ? asciiKeyword : normalized;
        }
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
            throw new IOException("城市搜索参数缺失：" + fieldName);
        }
        return value;
    }
}
