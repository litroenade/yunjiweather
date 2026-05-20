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

    @Query("SELECT * FROM warning WHERE ownerUserId = :ownerUserId AND locationId = :locationId ORDER BY publishTime DESC")
    List<WarningEntity> findByLocationId(long ownerUserId, String locationId);

    @Query("SELECT * FROM warning WHERE ownerUserId = :ownerUserId AND locationId = :locationId AND warningId = :warningId LIMIT 1")
    WarningEntity findByWarningId(long ownerUserId, String locationId, String warningId);

    @Query("SELECT * FROM warning WHERE ownerUserId = :ownerUserId AND isNotified = 0 ORDER BY publishTime DESC")
    List<WarningEntity> findUnnotifiedWarnings(long ownerUserId);

    @Query("UPDATE warning SET isNotified = 1 WHERE ownerUserId = :ownerUserId AND locationId = :locationId AND warningId = :warningId")
    void markNotified(long ownerUserId, String locationId, String warningId);

    @Query("UPDATE warning SET isRead = 1 WHERE ownerUserId = :ownerUserId AND locationId = :locationId AND warningId = :warningId")
    void markRead(long ownerUserId, String locationId, String warningId);

    @Query("SELECT COUNT(*) FROM warning WHERE ownerUserId = :ownerUserId")
    int count(long ownerUserId);
}
