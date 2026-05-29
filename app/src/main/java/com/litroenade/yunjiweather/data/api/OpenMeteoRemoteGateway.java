package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.api.model.OpenMeteoAirQualityResponse;
import com.litroenade.yunjiweather.data.api.model.OpenMeteoForecastResponse;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.model.WeatherDailyData;
import com.litroenade.yunjiweather.data.model.WeatherHourlyData;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;
import com.litroenade.yunjiweather.utils.AirQualityUtils;
import com.litroenade.yunjiweather.utils.DateTimeUtils;
import com.litroenade.yunjiweather.utils.WeatherAdviceUtils;
import com.litroenade.yunjiweather.utils.WeatherCodeMapper;
import com.litroenade.yunjiweather.utils.WindScaleUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Response;

/**
 * 将开放气象数据源的天气和空气质量响应转换为首页展示模型。
 * 必要字段缺失时主动抛错，由仓库层决定是否回退到本地缓存。
 */
public final class OpenMeteoRemoteGateway implements WeatherRepository.RemoteGateway {

    private static final String TIMEZONE = "Asia/Shanghai";
    private static final String WIND_SPEED_UNIT = "kmh";
    private static final String CURRENT_WEATHER_FIELDS = "temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,pressure_msl,wind_speed_10m,wind_direction_10m,visibility";
    private static final String HOURLY_WEATHER_FIELDS = "temperature_2m,weather_code";
    private static final String DAILY_WEATHER_FIELDS = "weather_code,temperature_2m_max,temperature_2m_min,uv_index_max,sunrise,sunset";
    private static final String AIR_QUALITY_FIELDS = "us_aqi,us_aqi_pm2_5,us_aqi_pm10,us_aqi_nitrogen_dioxide,us_aqi_ozone,us_aqi_sulphur_dioxide,us_aqi_carbon_monoxide";

    private final OpenMeteoApiService forecastService;
    private final OpenMeteoApiService airQualityService;

    public OpenMeteoRemoteGateway(OpenMeteoApiService forecastService, OpenMeteoApiService airQualityService) {
        this.forecastService = forecastService;
        this.airQualityService = airQualityService;
    }

    @Override
    public HomeWeatherData fetchHomeWeather(String locationId, String cityName, double latitude, double longitude) throws IOException {
        OpenMeteoForecastResponse forecastResponse = executeForecast(latitude, longitude);
        OpenMeteoAirQualityResponse airQualityResponse = executeAirQuality(latitude, longitude);
        OpenMeteoForecastResponse.Current current = requireNonNull(forecastResponse.current, "current");
        OpenMeteoForecastResponse.Daily daily = requireNonNull(forecastResponse.daily, "daily");
        OpenMeteoAirQualityResponse.Current airCurrent = requireNonNull(airQualityResponse.current, "air.current");

        int weatherCode = requireInt(current.weather_code, "current.weather_code");
        boolean isDay = requireInt(current.is_day, "current.is_day") == 1;
        int temperature = roundToInt(requireDouble(current.temperature_2m, "current.temperature_2m"));
        int windScale = WindScaleUtils.toWindScale(requireDouble(current.wind_speed_10m, "current.wind_speed_10m"));
        int uvIndex = roundToInt(firstRequiredDouble(daily.uv_index_max, "daily.uv_index_max"));
        int airQualityIndex = roundToInt(requireDouble(airCurrent.us_aqi, "air.current.us_aqi"));
        String condition = WeatherCodeMapper.toCondition(weatherCode);

        String clothingAdvice = WeatherAdviceUtils.generateClothingAdvice(
                temperature,
                condition,
                windScale,
                airQualityIndex
        );
        String travelAdvice = WeatherAdviceUtils.generateTravelAdvice(
                temperature,
                condition,
                windScale,
                airQualityIndex,
                uvIndex,
                false
        );

        return new HomeWeatherData(
                requireText(cityName, "cityName"),
                requireText(locationId, "locationId"),
                String.valueOf(temperature),
                condition,
                formatRounded(requireDouble(current.apparent_temperature, "current.apparent_temperature")),
                formatRounded(firstRequiredDouble(daily.temperature_2m_max, "daily.temperature_2m_max")),
                formatRounded(firstRequiredDouble(daily.temperature_2m_min, "daily.temperature_2m_min")),
                formatRounded(requireDouble(current.relative_humidity_2m, "current.relative_humidity_2m")),
                WindScaleUtils.toWindDirectionText(requireDouble(current.wind_direction_10m, "current.wind_direction_10m")),
                String.valueOf(windScale),
                formatOneDecimal(requireDouble(current.wind_speed_10m, "current.wind_speed_10m")),
                formatRounded(requireDouble(current.pressure_msl, "current.pressure_msl")),
                formatOneDecimal(requireDouble(current.visibility, "current.visibility") / 1000.0d),
                WeatherCodeMapper.toIconCode(weatherCode, isDay),
                DateTimeUtils.parseOpenMeteoLocalTime(requireText(current.time, "current.time")),
                clothingAdvice,
                travelAdvice,
                String.valueOf(airQualityIndex),
                AirQualityUtils.toUsAqiCategory(airQualityIndex),
                AirQualityUtils.findPrimaryPollutant(
                        airCurrent.us_aqi_pm2_5,
                        airCurrent.us_aqi_pm10,
                        airCurrent.us_aqi_nitrogen_dioxide,
                        airCurrent.us_aqi_ozone,
                        airCurrent.us_aqi_sulphur_dioxide,
                        airCurrent.us_aqi_carbon_monoxide
                ),
                String.valueOf(uvIndex),
                DateTimeUtils.formatOpenMeteoHour(firstRequiredText(daily.sunrise, "daily.sunrise")),
                DateTimeUtils.formatOpenMeteoHour(firstRequiredText(daily.sunset, "daily.sunset")),
                mapHourlyForecasts(requireNonNull(forecastResponse.hourly, "hourly")),
                mapDailyForecasts(daily)
        );
    }

    private OpenMeteoForecastResponse executeForecast(double latitude, double longitude) throws IOException {
        Response<OpenMeteoForecastResponse> response = forecastService.getForecast(
                latitude,
                longitude,
                CURRENT_WEATHER_FIELDS,
                HOURLY_WEATHER_FIELDS,
                DAILY_WEATHER_FIELDS,
                TIMEZONE,
                WIND_SPEED_UNIT,
                7,
                24
        ).execute();
        OpenMeteoForecastResponse body = response.body();
        if (!response.isSuccessful() || body == null) {
            throw new IOException("Open-Meteo 天气接口请求失败");
        }
        return body;
    }

    private OpenMeteoAirQualityResponse executeAirQuality(double latitude, double longitude) throws IOException {
        Response<OpenMeteoAirQualityResponse> response = airQualityService.getAirQuality(
                latitude,
                longitude,
                AIR_QUALITY_FIELDS,
                TIMEZONE,
                1
        ).execute();
        OpenMeteoAirQualityResponse body = response.body();
        if (!response.isSuccessful() || body == null) {
            throw new IOException("Open-Meteo 空气质量接口请求失败");
        }
        return body;
    }

    private List<WeatherHourlyData> mapHourlyForecasts(OpenMeteoForecastResponse.Hourly hourly) throws IOException {
        List<WeatherHourlyData> result = new ArrayList<>();
        int maxCount = minRequiredSize("hourly", hourly.time, hourly.temperature_2m, hourly.weather_code);
        maxCount = Math.min(maxCount, 12);
        for (int i = 0; i < maxCount; i++) {
            int code = requireInt(hourly.weather_code.get(i), "hourly.weather_code[" + i + "]");
            result.add(new WeatherHourlyData(
                    DateTimeUtils.formatOpenMeteoHour(requireText(hourly.time.get(i), "hourly.time[" + i + "]")),
                    formatRounded(requireDouble(hourly.temperature_2m.get(i), "hourly.temperature_2m[" + i + "]")),
                    WeatherCodeMapper.toCondition(code),
                    WeatherCodeMapper.toIconCode(code, true)
            ));
        }
        return result;
    }

    private List<WeatherDailyData> mapDailyForecasts(OpenMeteoForecastResponse.Daily daily) throws IOException {
        List<WeatherDailyData> result = new ArrayList<>();
        int maxCount = minRequiredSize("daily", daily.time, daily.temperature_2m_max, daily.temperature_2m_min, daily.weather_code);
        maxCount = Math.min(maxCount, 7);
        for (int i = 0; i < maxCount; i++) {
            int code = requireInt(daily.weather_code.get(i), "daily.weather_code[" + i + "]");
            result.add(new WeatherDailyData(
                    DateTimeUtils.formatOpenMeteoDate(requireText(daily.time.get(i), "daily.time[" + i + "]")),
                    formatRounded(requireDouble(daily.temperature_2m_max.get(i), "daily.temperature_2m_max[" + i + "]")),
                    formatRounded(requireDouble(daily.temperature_2m_min.get(i), "daily.temperature_2m_min[" + i + "]")),
                    WeatherCodeMapper.toCondition(code),
                    WeatherCodeMapper.toIconCode(code, true)
            ));
        }
        return result;
    }

    @SafeVarargs
    private static int minRequiredSize(String fieldName, List<?>... lists) throws IOException {
        int minSize = Integer.MAX_VALUE;
        for (List<?> list : lists) {
            if (list == null || list.isEmpty()) {
                throw new IOException("Open-Meteo 接口缺少字段：" + fieldName);
            }
            minSize = Math.min(minSize, list.size());
        }
        return minSize;
    }

    private static <T> T requireNonNull(T value, String fieldName) throws IOException {
        try {
            return Objects.requireNonNull(value, fieldName);
        } catch (NullPointerException exception) {
            throw new IOException("Open-Meteo 接口缺少字段：" + fieldName, exception);
        }
    }

    private static String requireText(String value, String fieldName) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            throw new IOException("Open-Meteo 接口缺少字段：" + fieldName);
        }
        return value;
    }

    private static double requireDouble(Double value, String fieldName) throws IOException {
        if (value == null) {
            throw new IOException("Open-Meteo 接口缺少字段：" + fieldName);
        }
        return value;
    }

    private static int requireInt(Integer value, String fieldName) throws IOException {
        if (value == null) {
            throw new IOException("Open-Meteo 接口缺少字段：" + fieldName);
        }
        return value;
    }

    private static double firstRequiredDouble(List<Double> values, String fieldName) throws IOException {
        if (values == null || values.isEmpty() || values.get(0) == null) {
            throw new IOException("Open-Meteo 接口缺少字段：" + fieldName);
        }
        return values.get(0);
    }

    private static String firstRequiredText(List<String> values, String fieldName) throws IOException {
        if (values == null || values.isEmpty()) {
            throw new IOException("Open-Meteo 接口缺少字段：" + fieldName);
        }
        return requireText(values.get(0), fieldName + "[0]");
    }

    private static int roundToInt(double value) {
        return (int) Math.round(value);
    }

    private static String formatRounded(double value) {
        return String.valueOf(roundToInt(value));
    }

    private static String formatOneDecimal(double value) {
        return String.format(Locale.CHINA, "%.1f", value);
    }
}
