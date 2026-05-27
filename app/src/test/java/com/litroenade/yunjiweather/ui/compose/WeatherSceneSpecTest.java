package com.litroenade.yunjiweather.ui.compose;

import com.litroenade.yunjiweather.utils.WeatherIconUtils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WeatherSceneSpecTest {

    @Test
    public void fromIconCode_returnsSunnySceneWithoutPrecipitation() {
        WeatherSceneSpec spec = WeatherSceneSpec.fromIconCode("100");

        assertEquals(WeatherIconUtils.WeatherCategory.SUNNY, spec.getCategory());
        assertEquals(WeatherSceneSpec.Precipitation.NONE, spec.getPrecipitation());
        assertTrue(spec.hasCelestialGlow());
        assertTrue(spec.getCloudOpacity() < 0.30f);
        assertTrue(spec.getAtmosphereAlpha() > 0.15f);
    }

    @Test
    public void fromIconCode_returnsRainSceneWithDenseClouds() {
        WeatherSceneSpec spec = WeatherSceneSpec.fromIconCode("305");

        assertEquals(WeatherIconUtils.WeatherCategory.RAIN, spec.getCategory());
        assertEquals(WeatherSceneSpec.Precipitation.RAIN, spec.getPrecipitation());
        assertTrue(spec.getCloudOpacity() > 0.50f);
        assertTrue(spec.getParticleCount() >= 40);
    }

    @Test
    public void fromIconCode_returnsSnowSceneWithSnowParticles() {
        WeatherSceneSpec spec = WeatherSceneSpec.fromIconCode("401");

        assertEquals(WeatherIconUtils.WeatherCategory.SNOW, spec.getCategory());
        assertEquals(WeatherSceneSpec.Precipitation.SNOW, spec.getPrecipitation());
        assertTrue(spec.getParticleCount() >= 30);
    }

    @Test
    public void fromIconCode_returnsNightSceneWithDarkForeground() {
        WeatherSceneSpec spec = WeatherSceneSpec.fromIconCode("150");

        assertEquals(WeatherIconUtils.WeatherCategory.NIGHT, spec.getCategory());
        assertEquals(WeatherSceneSpec.Precipitation.NONE, spec.getPrecipitation());
        assertTrue(spec.hasCelestialGlow());
        assertTrue(spec.usesLightForeground());
    }

    @Test
    public void fromIconCode_usesCloudyFallbackForMissingIconCode() {
        WeatherSceneSpec spec = WeatherSceneSpec.fromIconCode(null);

        assertEquals(WeatherIconUtils.WeatherCategory.CLOUDY, spec.getCategory());
        assertEquals(WeatherSceneSpec.Precipitation.NONE, spec.getPrecipitation());
        assertTrue(spec.getCloudOpacity() >= 0.35f);
    }
}
