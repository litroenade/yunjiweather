package com.litroenade.yunjiweather.data.api;

import com.litroenade.yunjiweather.data.api.model.QWeatherAirQualityResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherDailyResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherHourlyResponse;
import com.litroenade.yunjiweather.data.api.model.QWeatherNowResponse;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;
import com.litroenade.yunjiweather.data.model.WeatherDailyData;
import com.litroenade.yunjiweather.data.model.WeatherHourlyData;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;
import com.litroenade.yunjiweather.utils.AirQualityUtils;
import com.litroenade.yunjiweather.utils.DateTimeUtils;
import com.litroenade.yunjiweather.utils.WeatherAdviceUtils;
import com.litroenade.yunjiweather.utils.WindScaleUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Response;

public final class QWeatherRemoteGateway implements WeatherRepository.RemoteGateway {

    private static final String SUCCESS_CODE = "200";

    private final WeatherApiService apiService;

    public QWeatherRemoteGateway(WeatherApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public HomeWeatherData fetchHomeWeather(String locationId, String cityName, double latitude, double longitude) throws IOException {
        if (!ApiConfig.isConfigured()) {
            throw new IOException("QWeather 配置缺失");
        }

        QWeatherNowResponse nowResponse = executeNow(locationId);
        QWeatherDailyResponse dailyResponse = executeDaily(locationId);
        QWeatherHourlyResponse hourlyResponse = executeHourly(locationId);
        QWeatherAirQualityResponse.AirIndex airIndex = executeAirQuality(latitude, longitude);
        QWeatherNowResponse.Now now = requireNonNull(nowResponse.now, "now");
        QWeatherDailyResponse.Daily today = requireNonNull(dailyResponse.daily.get(0), "today");

        int temperature = parseRequiredInt(now.temp, "now.temp");
        int windScale = parseWindScale(now.windScale);
        int uvIndex = parseRequiredInt(today.uvIndex, "daily.uvIndex");
        int airQualityIndex = parseAqiAsInt(airIndex.aqiDisplay, "air.indexes.aqiDisplay");
        long updateTime = DateTimeUtils.parseQWeatherTime(requireText(nowResponse.updateTime, "updateTime"));

        String clothingAdvice = WeatherAdviceUtils.generateClothingAdvice(
                temperature,
                now.text,
                windScale,
                airQualityIndex
        );
        String travelAdvice = WeatherAdviceUtils.generateTravelAdvice(
                temperature,
                now.text,
                windScale,
                airQualityIndex,
                uvIndex,
                false
        );

        return new HomeWeatherData(
                cityName,
                locationId,
                requireText(now.temp, "now.temp"),
                requireText(now.text, "now.text"),
                requireText(now.feelsLike, "now.feelsLike"),
                requireText(today.tempMax, "daily.tempMax"),
                requireText(today.tempMin, "daily.tempMin"),
                requireText(now.humidity, "now.humidity"),
                requireText(now.windDir, "now.windDir"),
                requireText(now.windScale, "now.windScale"),
                requireText(now.windSpeed, "now.windSpeed"),
                requireText(now.pressure, "now.pressure"),
                requireText(now.vis, "now.vis"),
                requireText(now.icon, "now.icon"),
                updateTime,
                clothingAdvice,
                travelAdvice,
                String.valueOf(airQualityIndex),
                requireText(airIndex.category, "air.indexes.category"),
                resolvePrimaryPollutantName(airIndex),
                requireText(today.uvIndex, "daily.uvIndex"),
                requireText(today.sunrise, "daily.sunrise"),
                requireText(today.sunset, "daily.sunset"),
                mapHourlyForecasts(hourlyResponse.hourly),
                mapDailyForecasts(dailyResponse.daily)
        );
    }

    private QWeatherNowResponse executeNow(String locationId) throws IOException {
        Response<QWeatherNowResponse> response = apiService.getNowWeather(locationId, "zh", "m").execute();
        QWeatherNowResponse body = response.body();
        if (!response.isSuccessful() || body == null || !SUCCESS_CODE.equals(body.code)) {
            throw new IOException("实时天气接口请求失败");
        }
        return body;
    }

    private QWeatherDailyResponse executeDaily(String locationId) throws IOException {
        Response<QWeatherDailyResponse> response = apiService.getDailyWeather(locationId, "zh", "m").execute();
        QWeatherDailyResponse body = response.body();
        if (!response.isSuccessful() || body == null || !SUCCESS_CODE.equals(body.code)) {
            throw new IOException("天气预报接口请求失败");
        }
        if (body.daily == null || body.daily.isEmpty()) {
            throw new IOException("天气预报接口缺少 daily 数据");
        }
        return body;
    }

    private QWeatherHourlyResponse executeHourly(String locationId) throws IOException {
        Response<QWeatherHourlyResponse> response = apiService.getHourlyWeather(locationId, "zh", "m").execute();
        QWeatherHourlyResponse body = response.body();
        if (!response.isSuccessful() || body == null || !SUCCESS_CODE.equals(body.code)) {
            throw new IOException("逐小时天气接口请求失败");
        }
        if (body.hourly == null || body.hourly.isEmpty()) {
            throw new IOException("逐小时天气接口缺少 hourly 数据");
        }
        return body;
    }

    private QWeatherAirQualityResponse.AirIndex executeAirQuality(double latitude, double longitude) throws IOException {
        Response<QWeatherAirQualityResponse> response = apiService.getCurrentAirQuality(
                formatCoordinate(latitude),
                formatCoordinate(longitude),
                "zh"
        ).execute();
        QWeatherAirQualityResponse body = response.body();
        if (!response.isSuccessful() || body == null || body.indexes == null || body.indexes.isEmpty()) {
            throw new IOException("空气质量接口请求失败");
        }
        for (QWeatherAirQualityResponse.AirIndex index : body.indexes) {
            if (index != null && ("cn-mee".equals(index.code) || "cn-mee-1h".equals(index.code))) {
                return index;
            }
        }
        QWeatherAirQualityResponse.AirIndex firstIndex = body.indexes.get(0);
        if (firstIndex == null) {
            throw new IOException("空气质量接口缺少 indexes 数据");
        }
        return firstIndex;
    }

    private List<WeatherHourlyData> mapHourlyForecasts(List<QWeatherHourlyResponse.Hourly> hourlyList) throws IOException {
        List<WeatherHourlyData> result = new ArrayList<>();
        int maxCount = Math.min(hourlyList.size(), 12);
        for (int i = 0; i < maxCount; i++) {
            QWeatherHourlyResponse.Hourly hourly = requireNonNull(hourlyList.get(i), "hourly[" + i + "]");
            result.add(new WeatherHourlyData(
                    DateTimeUtils.formatQWeatherHour(requireText(hourly.fxTime, "hourly.fxTime")),
                    requireText(hourly.temp, "hourly.temp"),
                    requireText(hourly.text, "hourly.text"),
                    requireText(hourly.icon, "hourly.icon")
            ));
        }
        return result;
    }

    private List<WeatherDailyData> mapDailyForecasts(List<QWeatherDailyResponse.Daily> dailyList) throws IOException {
        List<WeatherDailyData> result = new ArrayList<>();
        int maxCount = Math.min(dailyList.size(), 7);
        for (int i = 0; i < maxCount; i++) {
            QWeatherDailyResponse.Daily daily = requireNonNull(dailyList.get(i), "daily[" + i + "]");
            result.add(new WeatherDailyData(
                    DateTimeUtils.formatQWeatherDate(requireText(daily.fxDate, "daily.fxDate")),
                    requireText(daily.tempMax, "daily.tempMax"),
                    requireText(daily.tempMin, "daily.tempMin"),
                    requireText(daily.textDay, "daily.textDay"),
                    requireText(daily.iconDay, "daily.iconDay")
            ));
        }
        return result;
    }

    private static <T> T requireNonNull(T value, String fieldName) throws IOException {
        try {
            return Objects.requireNonNull(value, fieldName);
        } catch (NullPointerException exception) {
            throw new IOException("天气接口缺少字段：" + fieldName, exception);
        }
    }

    private static String requireText(String value, String fieldName) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            throw new IOException("天气接口缺少字段：" + fieldName);
        }
        return value;
    }

    static String resolvePrimaryPollutantName(QWeatherAirQualityResponse.AirIndex airIndex) throws IOException {
        QWeatherAirQualityResponse.PrimaryPollutant pollutant = airIndex.primaryPollutant;
        if (pollutant == null) {
            return "无";
        }
        if (pollutant.name != null && !pollutant.name.trim().isEmpty()) {
            return pollutant.name;
        }
        if (pollutant.fullName != null && !pollutant.fullName.trim().isEmpty()) {
            return pollutant.fullName;
        }
        if (pollutant.code != null && !pollutant.code.trim().isEmpty()) {
            return pollutant.code;
        }
        return "无";
    }

    private static int parseRequiredInt(String value, String fieldName) throws IOException {
        try {
            return Integer.parseInt(requireText(value, fieldName));
        } catch (NumberFormatException exception) {
            throw new IOException("天气接口字段格式错误：" + fieldName, exception);
        }
    }

    private static int parseAqiAsInt(String value, String fieldName) throws IOException {
        try {
            return AirQualityUtils.parseUsAqiDisplay(requireText(value, fieldName));
        } catch (IllegalArgumentException exception) {
            throw new IOException("空气质量接口字段格式错误：" + fieldName, exception);
        }
    }

    private static int parseWindScale(String value) throws IOException {
        try {
            return WindScaleUtils.parseDisplayScale(requireText(value, "now.windScale"));
        } catch (IllegalArgumentException exception) {
            throw new IOException("风力等级格式错误", exception);
        }
    }

    private static String formatCoordinate(double coordinate) {
        return String.format(Locale.US, "%.2f", coordinate);
    }
}
