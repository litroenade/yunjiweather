package com.litroenade.yunjiweather.ui.city;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.model.CityWeatherSummary;
import com.litroenade.yunjiweather.utils.WeatherDisplayUtils;

import java.util.Objects;

public final class CitySummaryFormatter {

    private CitySummaryFormatter() {
    }

    public static String format(CityEntity city, boolean isDefault, CityWeatherSummary summary, String temperatureUnit) {
        CityEntity safeCity = Objects.requireNonNull(city, "city");
        String cityMeta = safeCity.province + " · " + safeCity.country + (isDefault ? " · 默认城市" : "");
        if (summary == null) {
            return cityMeta + "\n天气摘要加载中";
        }
        if (!summary.getErrorMessage().isEmpty()) {
            return cityMeta + "\n" + summary.getErrorMessage();
        }
        String cacheText = summary.isFromCache() ? " · 缓存" : "";
        return cityMeta
                + "\n"
                + summary.getCondition()
                + " "
                + WeatherDisplayUtils.formatTemperature(summary.getTemperature(), temperatureUnit)
                + "  今日 "
                + WeatherDisplayUtils.formatTemperature(summary.getTempMin(), temperatureUnit)
                + " / "
                + WeatherDisplayUtils.formatTemperature(summary.getTempMax(), temperatureUnit)
                + cacheText;
    }
}
