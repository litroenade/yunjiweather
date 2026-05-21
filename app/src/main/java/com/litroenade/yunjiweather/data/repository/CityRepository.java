package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.CityDao;
import com.litroenade.yunjiweather.utils.DefaultCityUtils;

import java.util.List;

public final class CityRepository {

    private final long ownerUserId;
    private final CityDao cityDao;

    public CityRepository(long ownerUserId, CityDao cityDao) {
        this.ownerUserId = ownerUserId;
        this.cityDao = cityDao;
    }

    public CityEntity resolveDefaultCity(long nowTime) {
        return DefaultCityUtils.resolveDefaultCity(cityDao, ownerUserId, nowTime);
    }

    public List<CityEntity> findAll() {
        return cityDao.findAll(ownerUserId);
    }

    public CityEntity findDefaultCity() {
        return cityDao.findDefaultCity(ownerUserId);
    }

    public CityEntity findByLocationId(String locationId) {
        return cityDao.findByLocationId(ownerUserId, locationId);
    }

    public int count() {
        return cityDao.count(ownerUserId);
    }

    public void insert(CityEntity city) {
        city.ownerUserId = ownerUserId;
        cityDao.insert(city);
    }

    public void setDefaultCity(String locationId, long nowTime) {
        cityDao.clearDefaultCity(ownerUserId);
        cityDao.setDefaultCity(ownerUserId, locationId, nowTime);
    }

    public void deleteCity(CityEntity city, long nowTime) {
        cityDao.deleteByLocationId(ownerUserId, city.locationId);
        if (!city.isDefault) {
            return;
        }
        List<CityEntity> remainingCities = cityDao.findAll(ownerUserId);
        if (!remainingCities.isEmpty()) {
            setDefaultCity(remainingCities.get(0).locationId, nowTime);
        }
    }

    public void saveAsDefaultCity(CityEntity city, long nowTime) {
        city.ownerUserId = ownerUserId;
        CityEntity oldCity = cityDao.findByLocationId(ownerUserId, city.locationId);
        if (oldCity == null) {
            cityDao.clearDefaultCity(ownerUserId);
            cityDao.insert(city);
            return;
        }
        setDefaultCity(oldCity.locationId, nowTime);
    }
}
