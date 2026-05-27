package com.litroenade.yunjiweather.data.repository

import com.litroenade.yunjiweather.data.entity.CityEntity
import com.litroenade.yunjiweather.data.local.CityDao
import com.litroenade.yunjiweather.utils.DefaultCityUtils

class CityRepository(private val cityDao: CityDao) {

    fun resolveDefaultCity(nowTime: Long): CityEntity {
        return DefaultCityUtils.resolveDefaultCity(cityDao, nowTime)
    }

    fun findAll(): List<CityEntity> {
        return cityDao.findAll()
    }

    fun findDefaultCity(): CityEntity? {
        return cityDao.findDefaultCity()
    }

    fun findByLocationId(locationId: String): CityEntity? {
        return cityDao.findByLocationId(locationId)
    }

    fun count(): Int {
        return cityDao.count()
    }

    fun insert(city: CityEntity) {
        cityDao.insert(city)
    }

    fun setDefaultCity(locationId: String, nowTime: Long) {
        cityDao.clearDefaultCity()
        cityDao.setDefaultCity(locationId, nowTime)
    }

    fun deleteCity(city: CityEntity, nowTime: Long) {
        cityDao.deleteByLocationId(city.locationId)
        if (!city.isDefault) {
            return
        }
        val remainingCities = cityDao.findAll()
        if (remainingCities.isNotEmpty()) {
            setDefaultCity(remainingCities[0].locationId, nowTime)
        }
    }

    fun saveAsDefaultCity(city: CityEntity, nowTime: Long) {
        val oldCity = cityDao.findByLocationId(city.locationId)
        if (oldCity == null) {
            cityDao.clearDefaultCity()
            cityDao.insert(city)
            return
        }
        setDefaultCity(oldCity.locationId, nowTime)
    }
}
