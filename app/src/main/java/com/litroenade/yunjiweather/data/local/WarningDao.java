package com.litroenade.yunjiweather.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

import java.util.List;

@Dao
public interface WarningDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<WarningEntity> warnings);

    @Transaction
    default void replaceByLocation(String locationId, List<WarningEntity> warnings) {
        if (warnings.isEmpty()) {
            deleteByLocationId(locationId);
            return;
        }
        List<String> activeWarningIds = new java.util.ArrayList<>();
        for (WarningEntity warning : warnings) {
            activeWarningIds.add(warning.warningId);
        }
        deleteMissingByLocation(locationId, activeWarningIds);
        insertAll(warnings);
    }

    @Query("SELECT * FROM warning WHERE locationId = :locationId ORDER BY publishTime DESC")
    List<WarningEntity> findByLocationId(String locationId);

    @Query("SELECT * FROM warning WHERE locationId = :locationId AND warningId = :warningId LIMIT 1")
    WarningEntity findByWarningId(String locationId, String warningId);

    @Query("SELECT * FROM warning WHERE isNotified = 0 ORDER BY publishTime DESC")
    List<WarningEntity> findUnnotifiedWarnings();

    @Query("UPDATE warning SET isNotified = 1 WHERE locationId = :locationId AND warningId = :warningId")
    void markNotified(String locationId, String warningId);

    @Query("UPDATE warning SET isRead = 1 WHERE locationId = :locationId AND warningId = :warningId")
    void markRead(String locationId, String warningId);

    @Query("DELETE FROM warning WHERE locationId = :locationId AND warningId NOT IN (:activeWarningIds)")
    void deleteMissingByLocation(String locationId, List<String> activeWarningIds);

    @Query("DELETE FROM warning WHERE locationId = :locationId")
    void deleteByLocationId(String locationId);

    @Query("SELECT COUNT(*) FROM warning")
    int count();
}
