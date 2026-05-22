package com.litroenade.yunjiweather;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.litroenade.yunjiweather.auth.AuthSessionManager;
import com.litroenade.yunjiweather.data.entity.UserEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.ui.auth.AuthActivity;
import com.litroenade.yunjiweather.ui.splash.SplashActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.lifecycle.Lifecycle.State.DESTROYED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class YunjiWeatherInstrumentedTest {
    @Test
    public void targetContextUsesYunjiWeatherPackage() {
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
        assertNotNull(database.userDao());
    }

    @Test
    public void authActivityCanLaunch() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        appContext.getSharedPreferences(AuthSessionManager.SESSION_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();

        try (ActivityScenario<AuthActivity> scenario = ActivityScenario.launch(AuthActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void splashActivityCanLaunch() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        appContext.getSharedPreferences(AuthSessionManager.SESSION_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();

        try (ActivityScenario<SplashActivity> scenario = ActivityScenario.launch(SplashActivity.class)) {
            scenario.onActivity(activity -> assertNotNull(activity));
        }
    }

    @Test
    public void mainActivityRedirectsToAuthWhenSessionMissing() throws InterruptedException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        appContext.getSharedPreferences(AuthSessionManager.SESSION_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit();

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            Thread.sleep(300L);
            assertEquals(DESTROYED, scenario.getState());
        }
    }

    @Test
    public void mainActivityCanInitializeNavigationWithValidSession() throws InterruptedException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        AppDatabase database = AppDatabase.getInstance(appContext);
        long now = System.currentTimeMillis();
        String username = "navtest_" + now;
        UserEntity user = new UserEntity(username, "hash", "salt", "NavTest", now, now);
        long userId = database.userDao().insert(user);
        new AuthSessionManager(appContext).login(userId, username, "NavTest");
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            Thread.sleep(800L);
            scenario.onActivity(activity -> assertNotNull(activity.findViewById(R.id.nav_host_fragment_activity_main)));
        }
    }
}
