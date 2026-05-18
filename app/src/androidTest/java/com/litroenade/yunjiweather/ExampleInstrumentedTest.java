package com.litroenade.yunjiweather;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.litroenade.yunjiweather.data.local.AppDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.litroenade.yunjiweather", appContext.getPackageName());
    }

    @Test
    public void databaseCanCreateRoomDatabase() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AppDatabase database = AppDatabase.getInstance(appContext);

        assertNotNull(database.cityDao());
        assertNotNull(database.weatherCacheDao());
        assertNotNull(database.warningDao());
    }

    @Test
    public void mainActivityCanLaunch() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }
}
