package com.litroenade.yunjiweather.data.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class HomeWeatherData {

    private final String cityName;
    private final String locationId;
    private final String temperature;
    private final String condition;
    private final String feelsLike;
    private final String tempMax;
    private final String tempMin;
    private final String humidity;
    private final String windDir;
    private final String windScale;
    private final String windSpeed;
    private final String pressure;
    private final String visibility;
    private final String iconCode;
    private final long updateTime;
    private final String clothingAdvice;
    private final String travelAdvice;
    private final String airQualityIndex;
    private final String airQualityCategory;
    private final String primaryPollutant;
    private final String uvIndex;
    private final String sunrise;
    private final String sunset;
    private final List<WeatherHourlyData> hourlyForecasts;
    private final List<WeatherDailyData> dailyForecasts;

    public HomeWeatherData(
            String cityName,
            String locationId,
            String temperature,
            String condition,
            String feelsLike,
            String tempMax,
            String tempMin,
            String humidity,
            String windDir,
            String windScale,
            String windSpeed,
            String pressure,
            String visibility,
            String iconCode,
            long updateTime,
            String clothingAdvice,
            String travelAdvice,
            String airQualityIndex,
            String airQualityCategory,
            String primaryPollutant
    ) {
        this(
                cityName,
                locationId,
                temperature,
                condition,
                feelsLike,
                tempMax,
                tempMin,
                humidity,
                windDir,
                windScale,
                windSpeed,
                pressure,
                visibility,
                iconCode,
                updateTime,
                clothingAdvice,
                travelAdvice,
                airQualityIndex,
                airQualityCategory,
                primaryPollutant,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    public HomeWeatherData(
            String cityName,
            String locationId,
            String temperature,
            String condition,
            String feelsLike,
            String tempMax,
            String tempMin,
            String humidity,
            String windDir,
            String windScale,
            String windSpeed,
            String pressure,
            String visibility,
            String iconCode,
            long updateTime,
            String clothingAdvice,
            String travelAdvice,
            String airQualityIndex,
            String airQualityCategory,
            String primaryPollutant,
            List<WeatherHourlyData> hourlyForecasts,
            List<WeatherDailyData> dailyForecasts
    ) {
        this(
                cityName,
                locationId,
                temperature,
                condition,
                feelsLike,
                tempMax,
                tempMin,
                humidity,
                windDir,
                windScale,
                windSpeed,
                pressure,
                visibility,
                iconCode,
                updateTime,
                clothingAdvice,
                travelAdvice,
                airQualityIndex,
                airQualityCategory,
                primaryPollutant,
                "",
                "",
                "",
                hourlyForecasts,
                dailyForecasts
        );
    }

    public HomeWeatherData(
            String cityName,
            String locationId,
            String temperature,
            String condition,
            String feelsLike,
            String tempMax,
            String tempMin,
            String humidity,
            String windDir,
            String windScale,
            String windSpeed,
            String pressure,
            String visibility,
            String iconCode,
            long updateTime,
            String clothingAdvice,
            String travelAdvice,
            String airQualityIndex,
            String airQualityCategory,
            String primaryPollutant,
            String uvIndex,
            String sunrise,
            String sunset,
            List<WeatherHourlyData> hourlyForecasts,
            List<WeatherDailyData> dailyForecasts
    ) {
        this.cityName = requireText(cityName, "cityName");
        this.locationId = requireText(locationId, "locationId");
        this.temperature = requireText(temperature, "temperature");
        this.condition = requireText(condition, "condition");
        this.feelsLike = requireText(feelsLike, "feelsLike");
        this.tempMax = requireText(tempMax, "tempMax");
        this.tempMin = requireText(tempMin, "tempMin");
        this.humidity = requireText(humidity, "humidity");
        this.windDir = requireText(windDir, "windDir");
        this.windScale = requireText(windScale, "windScale");
        this.windSpeed = requireText(windSpeed, "windSpeed");
        this.pressure = requireText(pressure, "pressure");
        this.visibility = requireText(visibility, "visibility");
        this.iconCode = requireText(iconCode, "iconCode");
        this.updateTime = updateTime;
        this.clothingAdvice = requireText(clothingAdvice, "clothingAdvice");
        this.travelAdvice = requireText(travelAdvice, "travelAdvice");
        this.airQualityIndex = requireText(airQualityIndex, "airQualityIndex");
        this.airQualityCategory = requireText(airQualityCategory, "airQualityCategory");
        this.primaryPollutant = requireText(primaryPollutant, "primaryPollutant");
        this.uvIndex = optionalText(uvIndex);
        this.sunrise = optionalText(sunrise);
        this.sunset = optionalText(sunset);
        this.hourlyForecasts = Collections.unmodifiableList(Objects.requireNonNull(hourlyForecasts, "hourlyForecasts"));
        this.dailyForecasts = Collections.unmodifiableList(Objects.requireNonNull(dailyForecasts, "dailyForecasts"));
    }

    private static String requireText(String value, String fieldName) {
        String text = Objects.requireNonNull(value, fieldName);
        if (text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty");
        }
        return text;
    }

    private static String optionalText(String value) {
        return value == null ? "" : value.trim();
    }

    public void validateForDisplay() {
        requireText(cityName, "cityName");
        requireText(locationId, "locationId");
        requireText(temperature, "temperature");
        requireText(condition, "condition");
        requireText(feelsLike, "feelsLike");
        requireText(tempMax, "tempMax");
        requireText(tempMin, "tempMin");
        requireText(humidity, "humidity");
        requireText(windDir, "windDir");
        requireText(windScale, "windScale");
        requireText(windSpeed, "windSpeed");
        requireText(pressure, "pressure");
        requireText(visibility, "visibility");
        requireText(iconCode, "iconCode");
        requireText(clothingAdvice, "clothingAdvice");
        requireText(travelAdvice, "travelAdvice");
        requireText(airQualityIndex, "airQualityIndex");
        requireText(airQualityCategory, "airQualityCategory");
        requireText(primaryPollutant, "primaryPollutant");
        Objects.requireNonNull(hourlyForecasts, "hourlyForecasts");
        Objects.requireNonNull(dailyForecasts, "dailyForecasts");
    }

    public String getCityName() {
        return cityName;
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

    public String getFeelsLike() {
        return feelsLike;
    }

    public String getTempMax() {
        return tempMax;
    }

    public String getTempMin() {
        return tempMin;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getWindDir() {
        return windDir;
    }

    public String getWindScale() {
        return windScale;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getPressure() {
        return pressure;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getIconCode() {
        return iconCode;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public String getClothingAdvice() {
        return clothingAdvice;
    }

    public String getTravelAdvice() {
        return travelAdvice;
    }

    public String getAirQualityIndex() {
        return airQualityIndex;
    }

    public String getAirQualityCategory() {
        return airQualityCategory;
    }

    public String getPrimaryPollutant() {
        return primaryPollutant;
    }

    public String getUvIndex() {
        return optionalText(uvIndex);
    }

    public String getSunrise() {
        return optionalText(sunrise);
    }

    public String getSunset() {
        return optionalText(sunset);
    }

    public List<WeatherHourlyData> getHourlyForecasts() {
        return hourlyForecasts;
    }

    public List<WeatherDailyData> getDailyForecasts() {
        return dailyForecasts;
    }
}
