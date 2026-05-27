package com.litroenade.yunjiweather.data.repository;

import static org.junit.Assert.assertNotNull;

import com.litroenade.yunjiweather.data.local.AppDatabase;

import org.junit.Test;

public class WeatherRepositoryFactoryJavaInteropTest {

    @Test
    public void createHomeRepositoryKeepsJavaStaticFactoryEntryPoint() {
        assertNotNull(WeatherRepositoryFactory.class);
    }

    private static WeatherRepository createFromJava(AppDatabase database) {
        return WeatherRepositoryFactory.createHomeRepository(database, null);
    }
}
