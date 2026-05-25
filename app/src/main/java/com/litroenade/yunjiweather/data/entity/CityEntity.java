package com.litroenade.yunjiweather.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "city",
        indices = {
                @Index(value = {"locationId"}, unique = true)
        }
)
public class CityEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String cityName;

    @NonNull
    public String locationId;

    @NonNull
    public String province;

    @NonNull
    public String country;

    public double latitude;

    public double longitude;

    public boolean isDefault;

    public int sortOrder;

    public long createTime;

    public long updateTime;

    public CityEntity(
            @NonNull String cityName,
            @NonNull String locationId,
            @NonNull String province,
            @NonNull String country,
            double latitude,
            double longitude,
            boolean isDefault,
            int sortOrder,
            long createTime,
            long updateTime
    ) {
        this.cityName = cityName;
        this.locationId = locationId;
        this.province = province;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isDefault = isDefault;
        this.sortOrder = sortOrder;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
}
