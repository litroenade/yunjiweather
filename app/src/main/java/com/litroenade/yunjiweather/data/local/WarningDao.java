package com.litroenade.yunjiweather.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

import java.util.List;

@Dao
public interface WarningDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WarningEntity> warnings);

    @Query("SELECT * FROM warning WHERE locationId = :locationId ORDER BY publishTime DESC")
    List<WarningEntity> findByLocationId(String locationId);

    @Query("SELECT * FROM warning WHERE warningId = :warningId LIMIT 1")
    WarningEntity findByWarningId(String warningId);

    @Query("SELECT * FROM warning WHERE isNotified = 0 ORDER BY publishTime DESC")
    List<WarningEntity> findUnnotifiedWarnings();

    @Query("UPDATE warning SET isNotified = 1 WHERE warningId = :warningId")
    void markNotified(String warningId);

    @Query("UPDATE warning SET isRead = 1 WHERE warningId = :warningId")
    void markRead(String warningId);
}
