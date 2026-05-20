package com.litroenade.yunjiweather.utils;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.CityDao;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DefaultCityUtilsTest {

    @Test
    public void resolveDefaultCity_keepsExistingDefaultCityForCurrentUser() {
        FakeCityDao cityDao = new FakeCityDao();
        CityEntity shanghai = new CityEntity(1L, "上海", "101020100", "上海", "中国", 31.2304, 121.4737, true, 1, 10L, 10L);
        cityDao.insert(shanghai);
        cityDao.insert(new CityEntity(2L, "深圳", "101280601", "广东", "中国", 22.5431, 114.0579, true, 1, 10L, 10L));

        CityEntity result = DefaultCityUtils.resolveDefaultCity(cityDao, 1L, 99L);

        assertSame(shanghai, result);
        assertEquals(1, cityDao.findAll(1L).size());
        assertEquals(1, cityDao.findAll(2L).size());
    }

    @Test
    public void resolveDefaultCity_seedsBeijingOnlyForCurrentUserWhenEmpty() {
        FakeCityDao cityDao = new FakeCityDao();
        cityDao.insert(new CityEntity(2L, "深圳", "101280601", "广东", "中国", 22.5431, 114.0579, true, 1, 10L, 10L));

        CityEntity result = DefaultCityUtils.resolveDefaultCity(cityDao, 1L, 123L);

        assertEquals(1L, result.ownerUserId);
        assertEquals("北京", result.cityName);
        assertEquals("101010100", result.locationId);
        assertTrue(result.isDefault);
        assertEquals(123L, result.createTime);
        assertEquals(1, cityDao.findAll(1L).size());
        assertEquals(1, cityDao.findAll(2L).size());
    }

    @Test
    public void formatDefaultCityText_usesRoomCityName() {
        CityEntity city = new CityEntity(1L, "深圳", "101280601", "广东", "中国", 22.5431, 114.0579, true, 3, 1L, 2L);

        String text = DefaultCityUtils.formatDefaultCityText(city);

        assertEquals("默认城市：深圳", text);
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
            CityEntity city = findByLocationId(ownerUserId, locationId);
            if (city != null) {
                city.isDefault = true;
                city.updateTime = updateTime;
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
