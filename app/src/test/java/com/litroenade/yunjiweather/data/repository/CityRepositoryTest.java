package com.litroenade.yunjiweather.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.CityDao;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CityRepositoryTest {

    @Test
    public void deleteDefaultCityPromotesFirstRemainingCity() {
        FakeCityDao cityDao = new FakeCityDao();
        CityEntity beijing = city("北京", "101010100", true);
        CityEntity shanghai = city("上海", "101020100", false);
        cityDao.insert(beijing);
        cityDao.insert(shanghai);
        cityDao.insertedDuringSave = false;
        CityRepository repository = new CityRepository(cityDao);

        repository.deleteCity(beijing, 1716600000000L);

        assertEquals("101010100", cityDao.deletedLocationId);
        assertEquals("101020100", cityDao.defaultLocationId);
        assertEquals(1716600000000L, cityDao.defaultUpdateTime);
    }

    @Test
    public void saveNewCityAsDefaultClearsOldDefaultAndInsertsCity() {
        FakeCityDao cityDao = new FakeCityDao();
        CityEntity shenzhen = city("深圳", "101280601", true);
        CityRepository repository = new CityRepository(cityDao);

        repository.saveAsDefaultCity(shenzhen, 1716600000000L);

        assertTrue(cityDao.defaultCleared);
        assertEquals("101280601", cityDao.findByLocationId("101280601").locationId);
    }

    @Test
    public void saveExistingCityAsDefaultUsesExistingRecord() {
        FakeCityDao cityDao = new FakeCityDao();
        CityEntity guangzhou = city("广州", "101280101", false);
        cityDao.insert(guangzhou);
        cityDao.insertedDuringSave = false;
        CityRepository repository = new CityRepository(cityDao);

        repository.saveAsDefaultCity(city("广州", "101280101", true), 1716600000000L);

        assertEquals("101280101", cityDao.defaultLocationId);
        assertFalse(cityDao.insertedDuringSave);
    }

    @Test
    public void nullableFindMethodsReturnNullWhenDaoHasNoMatch() {
        CityRepository repository = new CityRepository(new FakeCityDao());

        assertNull(repository.findDefaultCity());
        assertNull(repository.findByLocationId("missing"));
    }

    @Test
    public void deleteNonDefaultCityDoesNotPromoteFallback() {
        FakeCityDao cityDao = new FakeCityDao();
        CityEntity beijing = city("北京", "101010100", true);
        CityEntity shanghai = city("上海", "101020100", false);
        cityDao.insert(beijing);
        cityDao.insert(shanghai);
        CityRepository repository = new CityRepository(cityDao);

        repository.deleteCity(shanghai, 1716600000000L);

        assertEquals("101020100", cityDao.deletedLocationId);
        assertNull(cityDao.defaultLocationId);
    }

    @Test
    public void setDefaultCityClearsBeforeSettingNewDefault() {
        FakeCityDao cityDao = new FakeCityDao();
        cityDao.insert(city("北京", "101010100", true));
        cityDao.insert(city("上海", "101020100", false));
        CityRepository repository = new CityRepository(cityDao);

        repository.setDefaultCity("101020100", 1716600000000L);

        assertEquals("clear,set:101020100", String.join(",", cityDao.operationLog));
        assertEquals("101020100", cityDao.findDefaultCity().locationId);
    }

    private static CityEntity city(String cityName, String locationId, boolean isDefault) {
        return new CityEntity(cityName, locationId, cityName, "中国", 0.0, 0.0, isDefault, 0, 1L, 1L);
    }

    private static final class FakeCityDao implements CityDao {
        private final List<CityEntity> cities = new ArrayList<>();
        private boolean defaultCleared;
        private boolean insertedDuringSave;
        private String deletedLocationId;
        private String defaultLocationId;
        private long defaultUpdateTime;
        private final List<String> operationLog = new ArrayList<>();

        @Override
        public void insert(CityEntity city) {
            insertedDuringSave = true;
            cities.add(city);
        }

        @Override
        public List<CityEntity> findAll() {
            return new ArrayList<>(cities);
        }

        @Override
        public CityEntity findByLocationId(String locationId) {
            for (CityEntity city : cities) {
                if (city.locationId.equals(locationId)) {
                    return city;
                }
            }
            return null;
        }

        @Override
        public CityEntity findDefaultCity() {
            for (CityEntity city : cities) {
                if (city.isDefault) {
                    return city;
                }
            }
            return null;
        }

        @Override
        public void clearDefaultCity() {
            operationLog.add("clear");
            defaultCleared = true;
            for (CityEntity city : cities) {
                city.isDefault = false;
            }
        }

        @Override
        public void setDefaultCity(String locationId, long updateTime) {
            operationLog.add("set:" + locationId);
            defaultLocationId = locationId;
            defaultUpdateTime = updateTime;
            for (CityEntity city : cities) {
                city.isDefault = city.locationId.equals(locationId);
                if (city.isDefault) {
                    city.updateTime = updateTime;
                }
            }
        }

        @Override
        public void deleteByLocationId(String locationId) {
            deletedLocationId = locationId;
            cities.removeIf(city -> city.locationId.equals(locationId));
        }

        @Override
        public int count() {
            return cities.size();
        }
    }
}
