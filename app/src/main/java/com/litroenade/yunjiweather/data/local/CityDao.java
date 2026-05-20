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

    @Query("SELECT * FROM city WHERE ownerUserId = :ownerUserId ORDER BY isDefault DESC, sortOrder ASC, createTime ASC")
    List<CityEntity> findAll(long ownerUserId);

    @Query("SELECT * FROM city WHERE ownerUserId = :ownerUserId AND locationId = :locationId LIMIT 1")
    CityEntity findByLocationId(long ownerUserId, String locationId);

    @Query("SELECT * FROM city WHERE ownerUserId = :ownerUserId AND isDefault = 1 LIMIT 1")
    CityEntity findDefaultCity(long ownerUserId);

    @Query("UPDATE city SET isDefault = 0 WHERE ownerUserId = :ownerUserId")
    void clearDefaultCity(long ownerUserId);

    @Query("UPDATE city SET isDefault = 1, updateTime = :updateTime WHERE ownerUserId = :ownerUserId AND locationId = :locationId")
    void setDefaultCity(long ownerUserId, String locationId, long updateTime);

    @Query("DELETE FROM city WHERE ownerUserId = :ownerUserId AND locationId = :locationId")
    void deleteByLocationId(long ownerUserId, String locationId);

    @Query("SELECT COUNT(*) FROM city WHERE ownerUserId = :ownerUserId")
    int count(long ownerUserId);
}
