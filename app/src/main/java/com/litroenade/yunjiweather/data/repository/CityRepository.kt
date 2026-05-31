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

    fun moveCity(locationId: String, direction: Int, nowTime: Long) {
        if (direction == 0) {
            return
        }
        val cities = cityDao.findAll()
        val currentIndex = cities.indexOfFirst { city -> city.locationId == locationId }
        if (currentIndex < 0) {
            return
        }
        val city = cities[currentIndex]
        if (city.isDefault) {
            return
        }
        val targetIndex = (currentIndex + direction).coerceIn(0, cities.lastIndex)
        val targetCity = cities[targetIndex]
        if (targetIndex == currentIndex || targetCity.isDefault) {
            return
        }
        val currentSortOrder = city.sortOrder
        val targetSortOrder = targetCity.sortOrder
        cityDao.updateSortOrder(city.locationId, targetSortOrder, nowTime)
        cityDao.updateSortOrder(targetCity.locationId, currentSortOrder, nowTime)
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
