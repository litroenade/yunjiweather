package com.litroenade.yunjiweather.data.local;

import com.litroenade.yunjiweather.data.entity.CityEntity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UserScopedCityTest {

    @Test
    public void findAll_onlyReturnsCurrentUserCities() {
        FakeCityDao cityDao = new FakeCityDao();
        cityDao.insert(city(1L, "北京", "101010100", true));
        cityDao.insert(city(2L, "上海", "101020100", true));

        assertEquals(1, cityDao.findAll(1L).size());
        assertEquals("北京", cityDao.findAll(1L).get(0).cityName);
        assertEquals(1, cityDao.findAll(2L).size());
        assertEquals("上海", cityDao.findAll(2L).get(0).cityName);
    }

    @Test
    public void defaultCity_isolatedBetweenUsers() {
        FakeCityDao cityDao = new FakeCityDao();
        cityDao.insert(city(1L, "北京", "101010100", true));
        cityDao.insert(city(2L, "上海", "101020100", true));

        cityDao.setDefaultCity(1L, "101010100", 200L);

        assertEquals("北京", cityDao.findDefaultCity(1L).cityName);
        assertEquals("上海", cityDao.findDefaultCity(2L).cityName);
    }

    @Test
    public void sameUser_cannotFindDuplicateLocationAsDifferentRecord() {
        FakeCityDao cityDao = new FakeCityDao();
        CityEntity first = city(1L, "北京", "101010100", true);
        CityEntity second = city(1L, "北京", "101010100", false);

        cityDao.insert(first);
        cityDao.insert(second);

        assertEquals(1, cityDao.findAll(1L).size());
        assertEquals(second, cityDao.findByLocationId(1L, "101010100"));
        assertNull(cityDao.findByLocationId(2L, "101010100"));
    }

    private static CityEntity city(long ownerUserId, String cityName, String locationId, boolean isDefault) {
        return new CityEntity(ownerUserId, cityName, locationId, cityName, "中国", 1.0, 2.0, isDefault, 1, 100L, 100L);
    }

    private static final class FakeCityDao implements CityDao {
        private final List<CityEntity> cities = new ArrayList<>();

        @Override
        public void insert(CityEntity city) {
            CityEntity oldCity = findByLocationId(city.ownerUserId, city.locationId);
            if (oldCity != null) {
                cities.remove(oldCity);
            }
            cities.add(city);
        }

        @Override
        public List<CityEntity> findAll(long ownerUserId) {
            List<CityEntity> result = new ArrayList<>();
            for (CityEntity city : cities) {
                if (city.ownerUserId == ownerUserId) {
                    result.add(city);
                }
            }
            return result;
        }

        @Override
        public CityEntity findByLocationId(long ownerUserId, String locationId) {
            for (CityEntity city : cities) {
                if (city.ownerUserId == ownerUserId && city.locationId.equals(locationId)) {
                    return city;
                }
            }
            return null;
        }

        @Override
        public CityEntity findDefaultCity(long ownerUserId) {
            for (CityEntity city : cities) {
                if (city.ownerUserId == ownerUserId && city.isDefault) {
                    return city;
                }
            }
            return null;
        }

        @Override
        public void clearDefaultCity(long ownerUserId) {
            for (CityEntity city : cities) {
                if (city.ownerUserId == ownerUserId) {
                    city.isDefault = false;
                }
            }
        }

        @Override
        public void setDefaultCity(long ownerUserId, String locationId, long updateTime) {
            for (CityEntity city : cities) {
                if (city.ownerUserId == ownerUserId) {
                    city.isDefault = city.locationId.equals(locationId);
                    city.updateTime = updateTime;
                }
            }
        }

        @Override
        public void deleteByLocationId(long ownerUserId, String locationId) {
            CityEntity city = findByLocationId(ownerUserId, locationId);
            if (city != null) {
                cities.remove(city);
            }
        }

        @Override
        public int count(long ownerUserId) {
            return findAll(ownerUserId).size();
        }
    }
}
