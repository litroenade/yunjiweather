package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.local.CityDao;
import com.litroenade.yunjiweather.utils.DefaultCityUtils;

import java.util.List;

public final class CityRepository {

    private final CityDao cityDao;

    public CityRepository(CityDao cityDao) {
        this.cityDao = cityDao;
    }

    public CityEntity resolveDefaultCity(long nowTime) {
        return DefaultCityUtils.resolveDefaultCity(cityDao, nowTime);
    }

    public List<CityEntity> findAll() {
        return cityDao.findAll();
    }

    public CityEntity findDefaultCity() {
        return cityDao.findDefaultCity();
    }

    public CityEntity findByLocationId(String locationId) {
        return cityDao.findByLocationId(locationId);
    }

    public int count() {
        return cityDao.count();
    }

    public void insert(CityEntity city) {
        cityDao.insert(city);
    }

    public void setDefaultCity(String locationId, long nowTime) {
        cityDao.clearDefaultCity();
        cityDao.setDefaultCity(locationId, nowTime);
    }

    public void deleteCity(CityEntity city, long nowTime) {
        cityDao.deleteByLocationId(city.locationId);
        if (!city.isDefault) {
            return;
        }
        List<CityEntity> remainingCities = cityDao.findAll();
        if (!remainingCities.isEmpty()) {
            setDefaultCity(remainingCities.get(0).locationId, nowTime);
        }
    }

    public void saveAsDefaultCity(CityEntity city, long nowTime) {
        CityEntity oldCity = cityDao.findByLocationId(city.locationId);
        if (oldCity == null) {
            cityDao.clearDefaultCity();
            cityDao.insert(city);
            return;
        }
        setDefaultCity(oldCity.locationId, nowTime);
    }
}
