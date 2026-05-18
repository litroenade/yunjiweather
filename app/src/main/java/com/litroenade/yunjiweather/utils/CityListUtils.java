package com.litroenade.yunjiweather.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CityListUtils {

    private CityListUtils() {
    }

    public static AddResult addCity(List<String> currentCities, String cityName) {
        String normalized = normalizeCity(cityName);
        List<String> cities = normalizeCities(currentCities);
        if (cities.contains(normalized)) {
            return new AddResult(false, cities);
        }
        cities.add(normalized);
        return new AddResult(true, cities);
    }

    public static String resolveDefaultAfterRemove(List<String> citiesAfterRemove, String currentDefaultCity, String removedCity) {
        String normalizedDefault = normalizeCity(currentDefaultCity);
        String normalizedRemoved = normalizeCity(removedCity);
        List<String> cities = normalizeCities(citiesAfterRemove);
        if (!normalizedRemoved.equals(normalizedDefault)) {
            return normalizedDefault;
        }
        if (cities.isEmpty()) {
            throw new IllegalArgumentException("citiesAfterRemove must not be empty");
        }
        return cities.get(0);
    }

    public static List<String> normalizeCities(List<String> cities) {
        if (cities == null) {
            throw new IllegalArgumentException("cities must not be null");
        }
        Set<String> normalizedCities = new LinkedHashSet<>();
        for (String city : cities) {
            normalizedCities.add(normalizeCity(city));
        }
        return new ArrayList<>(normalizedCities);
    }

    public static String normalizeCity(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            throw new IllegalArgumentException("cityName must not be empty");
        }
        return cityName.trim();
    }

    public static final class AddResult {
        private final boolean added;
        private final List<String> cities;

        private AddResult(boolean added, List<String> cities) {
            this.added = added;
            this.cities = cities;
        }

        public boolean isAdded() {
            return added;
        }

        public List<String> getCities() {
            return cities;
        }
    }
}
