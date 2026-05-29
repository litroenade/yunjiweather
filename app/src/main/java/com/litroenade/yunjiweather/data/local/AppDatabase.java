package com.litroenade.yunjiweather.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity;
import com.litroenade.yunjiweather.data.entity.WarningEntity;

/**
 * 无账号版本只保留单一本地数据库。
 * 登录和用户维度表已移除，允许破坏性迁移是为了避免旧用户字段数据污染新结构。
 */
@Database(
        entities = {
                WeatherCacheEntity.class,
                CityEntity.class,
                WarningEntity.class
        },
        version = 5,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "yunji_weather.db"
                            )
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract WeatherCacheDao weatherCacheDao();

    public abstract CityDao cityDao();

    public abstract WarningDao warningDao();
}
