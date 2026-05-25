package com.litroenade.yunjiweather;

import android.content.Context;
import android.database.Cursor;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.ui.splash.SplashActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class YunjiWeatherInstrumentedTest {

    @Test
    public void targetContextUsesYunjiWeatherPackage() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.litroenade.yunjiweather", appContext.getPackageName());
    }

    @Test
    public void databaseCanCreateLocalSchema() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AppDatabase database = AppDatabase.getInstance(appContext);
        SupportSQLiteDatabase sqliteDatabase = database.getOpenHelper().getWritableDatabase();

        assertNotNull(database.cityDao());
        assertNotNull(database.weatherCacheDao());
        assertNotNull(database.warningDao());
        assertTableDoesNotExist(sqliteDatabase, "user");
        assertTableDoesNotHaveColumn(sqliteDatabase, "city", "ownerUserId");
        assertTableDoesNotHaveColumn(sqliteDatabase, "weather_cache", "ownerUserId");
        assertTableDoesNotHaveColumn(sqliteDatabase, "warning", "ownerUserId");
    }

    @Test
    public void splashActivityLaunchesDirectly() {
        try (ActivityScenario<SplashActivity> scenario = ActivityScenario.launch(SplashActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void mainActivityLaunchesDirectly() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    private static void assertTableDoesNotExist(SupportSQLiteDatabase database, String tableName) {
        try (Cursor cursor = database.query(
                "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
                new Object[]{tableName}
        )) {
            assertEquals(0, cursor.getCount());
        }
    }

    private static void assertTableDoesNotHaveColumn(
            SupportSQLiteDatabase database,
            String tableName,
            String columnName
    ) {
        try (Cursor cursor = database.query("PRAGMA table_info(`" + tableName + "`)")) {
            int nameColumnIndex = cursor.getColumnIndexOrThrow("name");
            while (cursor.moveToNext()) {
                assertFalse(columnName.equals(cursor.getString(nameColumnIndex)));
            }
        }
    }
}
