package com.litroenade.yunjiweather.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "warning",
        indices = {
                @Index(value = {"ownerUserId", "locationId", "warningId"}, unique = true),
                @Index(value = {"ownerUserId", "locationId"})
        }
)
public class WarningEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long ownerUserId;

    @NonNull
    public String warningId;

    @NonNull
    public String locationId;

    @NonNull
    public String title;

    @NonNull
    public String type;

    @NonNull
    public String level;

    @NonNull
    public String content;

    public long publishTime;

    public boolean isRead;

    public boolean isNotified;

    public WarningEntity(
            long ownerUserId,
            @NonNull String warningId,
            @NonNull String locationId,
            @NonNull String title,
            @NonNull String type,
            @NonNull String level,
            @NonNull String content,
            long publishTime,
            boolean isRead,
            boolean isNotified
    ) {
        this.ownerUserId = ownerUserId;
        this.warningId = warningId;
        this.locationId = locationId;
        this.title = title;
        this.type = type;
        this.level = level;
        this.content = content;
        this.publishTime = publishTime;
        this.isRead = isRead;
        this.isNotified = isNotified;
    }
}
