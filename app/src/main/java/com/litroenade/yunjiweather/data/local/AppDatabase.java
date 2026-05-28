package com.litroenade.yunjiweather.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.litroenade.yunjiweather.data.entity.CityEntity;
import com.litroenade.yunjiweather.data.entity.WeatherCacheEntity;
import com.litroenade.yunjiweather.data.entity.WarningEntity;

/**
 * Single local database for the account-free app. Destructive migration is intentional
 * because the login/user-scoped schema was removed during the Compose rewrite.
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
                            .fallbackToDestructiveMigration()
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
