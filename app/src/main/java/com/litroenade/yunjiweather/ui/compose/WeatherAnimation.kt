package com.litroenade.yunjiweather.ui.compose

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.litroenade.yunjiweather.ui.compose.theme.LocalThemeSkin
import com.litroenade.yunjiweather.ui.compose.theme.effects.ThemeWeatherEffectCatalog

@Composable
internal fun WeatherAnimation(
    sceneSpec: WeatherSceneSpec,
    modifier: Modifier = Modifier,
    lightContext: WeatherLightContext = WeatherLightContext.now("", ""),
    motionScale: Float = 1f
) {
    val skin = LocalThemeSkin.current
    val effect = ThemeWeatherEffectCatalog.getEffect(skin.key)
    val transition = rememberInfiniteTransition(label = "weather-animation")
    val durationMillis = (1800 / (motionScale * skin.weatherAnimationSpeed).coerceAtLeast(0.75f))
        .toInt()
        .coerceAtLeast(900)
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weather-animation-progress"
    )

    Canvas(modifier = modifier.testTag("weather-hero-animation")) {
        if (size.width <= 0f || size.height <= 0f) {
            return@Canvas
        }
        with(effect) {
            drawHero(sceneSpec, lightContext, progress, skin)
        }
    }
}

@Composable
internal fun WeatherAtmosphere(
    sceneSpec: WeatherSceneSpec,
    modifier: Modifier = Modifier,
    lightContext: WeatherLightContext = WeatherLightContext.now("", ""),
    immersion: Float = 1f
) {
    val skin = LocalThemeSkin.current
    val effect = ThemeWeatherEffectCatalog.getEffect(skin.key)
    val transition = rememberInfiniteTransition(label = "weather-atmosphere")
    val motionScale = (immersion * skin.weatherAnimationSpeed).coerceAtLeast(0.75f)
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = (9000 / motionScale).toInt().coerceAtLeast(5200), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weather-atmosphere-progress"
    )

    Canvas(modifier = modifier.testTag("weather-atmosphere-animation")) {
        if (size.width <= 0f || size.height <= 0f) {
            return@Canvas
        }
        with(effect) {
            drawAtmosphere(sceneSpec, lightContext, progress, immersion, skin)
        }
    }
}
