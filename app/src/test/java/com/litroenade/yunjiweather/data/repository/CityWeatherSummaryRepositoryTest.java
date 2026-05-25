package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.common.UiState;
import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.model.CityWeatherSummary;
import com.litroenade.yunjiweather.data.model.HomeWeatherData;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CityWeatherSummaryRepositoryTest {

    @Test
    public void loadSummaries_preservesCityOrderAndMapsWeatherState() {
        CityEntity beijing = city("Beijing", "101010100", 39.9042, 116.4074);
        CityEntity shanghai = city("Shanghai", "101020100", 31.2304, 121.4737);
        CityWeatherSummaryRepository repository = new CityWeatherSummaryRepository(
                new FakeHomeWeatherSource()
                        .withState("101010100", UiState.success(homeWeather("Beijing", "18", "Sunny")))
                        .withState("101020100", UiState.success(homeWeather("Shanghai", "25", "Cloudy")))
        );

        Map<String, CityWeatherSummary> summaries = repository.loadSummaries(Arrays.asList(beijing, shanghai));

        Iterator<String> keys = summaries.keySet().iterator();
        assertEquals("101010100", keys.next());
        assertEquals("101020100", keys.next());
        assertEquals("18", summaries.get("101010100").getTemperature());
        assertEquals("Sunny", summaries.get("101010100").getCondition());
        assertEquals("25", summaries.get("101020100").getTemperature());
        assertEquals("Cloudy", summaries.get("101020100").getCondition());
    }

    @Test
    public void loadSummaries_returnsUnavailableForErrorStateAndContinues() {
        CityEntity beijing = city("Beijing", "101010100", 39.9042, 116.4074);
        CityEntity shanghai = city("Shanghai", "101020100", 31.2304, 121.4737);
        CityWeatherSummaryRepository repository = new CityWeatherSummaryRepository(
                new FakeHomeWeatherSource()
                        .withState("101010100", UiState.error("network unavailable"))
                        .withState("101020100", UiState.success(homeWeather("Shanghai", "25", "Cloudy")))
        );

        Map<String, CityWeatherSummary> summaries = repository.loadSummaries(Arrays.asList(beijing, shanghai));

        assertEquals(
                CityWeatherSummary.unavailable("101010100").getErrorMessage(),
                summaries.get("101010100").getErrorMessage()
        );
        assertEquals("25", summaries.get("101020100").getTemperature());
    }

    @Test
    public void loadSummaries_publishesIncrementalSnapshots() {
        CityEntity beijing = city("Beijing", "101010100", 39.9042, 116.4074);
        CityEntity shanghai = city("Shanghai", "101020100", 31.2304, 121.4737);
        List<Map<String, CityWeatherSummary>> updates = new ArrayList<>();
        CityWeatherSummaryRepository repository = new CityWeatherSummaryRepository(
                new FakeHomeWeatherSource()
                        .withState("101010100", UiState.success(homeWeather("Beijing", "18", "Sunny")))
                        .withState("101020100", UiState.success(homeWeather("Shanghai", "25", "Cloudy")))
        );

        repository.loadSummaries(
                Arrays.asList(beijing, shanghai),
                updates::add
        );

        assertEquals(2, updates.size());
        assertEquals(1, updates.get(0).size());
        assertTrue(updates.get(0).containsKey("101010100"));
        assertFalse(updates.get(0).containsKey("101020100"));
        assertEquals(2, updates.get(1).size());
        assertEquals("Cloudy", updates.get(1).get("101020100").getCondition());
    }

    @Test
    public void loadSummaries_returnsEmptyMapForEmptyCities() {
        CityWeatherSummaryRepository repository = new CityWeatherSummaryRepository(new FakeHomeWeatherSource());

        Map<String, CityWeatherSummary> summaries = repository.loadSummaries(Collections.emptyList());

        assertTrue(summaries.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void loadSummaries_propagatesSourceRuntimeException() {
        CityWeatherSummaryRepository repository = new CityWeatherSummaryRepository(
                (locationId, cityName, latitude, longitude) -> {
                    throw new IllegalStateException("source bug");
                }
        );

        repository.loadSummaries(Collections.singletonList(city("Beijing", "101010100", 39.9042, 116.4074)));
    }

    private static CityEntity city(String cityName, String locationId, double latitude, double longitude) {
        return new CityEntity(cityName, locationId, cityName, "China", latitude, longitude, false, 0, 100L, 100L);
    }

    private static HomeWeatherData homeWeather(String cityName, String temperature, String condition) {
        return new HomeWeatherData(
                cityName,
                cityName,
                temperature,
                condition,
                "20",
                "28",
                "16",
                "60",
                "East",
                "2",
                "10",
                "1008",
                "18",
                "101",
                1_700_000_000_000L,
                "Wear a light jacket.",
                "Good for outdoor activity.",
                "42",
                "Good",
                "Today"
        );
    }

    private static final class FakeHomeWeatherSource implements HomeWeatherSource {

        private final Map<String, UiState<HomeWeatherData>> states = new java.util.LinkedHashMap<>();

        FakeHomeWeatherSource withState(String locationId, UiState<HomeWeatherData> state) {
            states.put(locationId, state);
            return this;
        }

        @Override
        public UiState<HomeWeatherData> loadHomeWeather(String locationId, String cityName, double latitude, double longitude) {
            return states.get(locationId);
        }
    }
}
