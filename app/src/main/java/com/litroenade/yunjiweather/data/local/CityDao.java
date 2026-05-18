package com.litroenade.yunjiweather.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.litroenade.yunjiweather.data.entity.CityEntity;

import java.util.List;

@Dao
public interface CityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CityEntity city);

    @Query("SELECT * FROM city ORDER BY isDefault DESC, sortOrder ASC, createTime ASC")
    List<CityEntity> findAll();

    @Query("SELECT * FROM city WHERE locationId = :locationId LIMIT 1")
    CityEntity findByLocationId(String locationId);

    @Query("SELECT * FROM city WHERE isDefault = 1 LIMIT 1")
    CityEntity findDefaultCity();

    @Query("UPDATE city SET isDefault = 0")
    void clearDefaultCity();

    @Query("UPDATE city SET isDefault = 1, updateTime = :updateTime WHERE locationId = :locationId")
    void setDefaultCity(String locationId, long updateTime);

    @Query("DELETE FROM city WHERE locationId = :locationId")
    void deleteByLocationId(String locationId);

    @Query("SELECT COUNT(*) FROM city")
    int count();
}
