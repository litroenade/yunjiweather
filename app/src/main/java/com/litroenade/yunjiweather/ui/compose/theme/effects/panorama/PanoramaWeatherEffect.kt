package com.litroenade.yunjiweather.ui.compose.theme.effects.panorama

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.ui.compose.WeatherLightContext
import com.litroenade.yunjiweather.ui.compose.WeatherSceneSpec
import com.litroenade.yunjiweather.ui.compose.theme.effects.ThemeWeatherEffect
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkin
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import com.litroenade.yunjiweather.utils.WeatherIconUtils
import kotlin.math.cos
import kotlin.math.sin

internal object PanoramaWeatherEffect : ThemeWeatherEffect {
    override val key = VisualThemeUtils.THEME_PANORAMA
    override val drawsHeroIcon = false

    override fun homeBackdropImageResId(sceneSpec: WeatherSceneSpec, lightContext: WeatherLightContext): Int {
        return when {
            lightContext.isNight || sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT -> R.drawable.theme_panorama_night
            sceneSpec.precipitation == WeatherSceneSpec.Precipitation.RAIN -> R.drawable.theme_panorama_rain
            sceneSpec.precipitation == WeatherSceneSpec.Precipitation.SNOW -> R.drawable.theme_panorama_snow
            else -> R.drawable.theme_panorama_day
        }
    }

    override fun homeBackdropAlpha(sceneSpec: WeatherSceneSpec, lightContext: WeatherLightContext): Float {
        return when {
            lightContext.isNight -> 0.32f
            sceneSpec.precipitation != WeatherSceneSpec.Precipitation.NONE -> 0.54f
            else -> 0.62f + lightContext.exposure * 0.08f
        }
    }

    override fun atmosphereLayerAlpha(skin: ThemeSkin): Float {
        return (0.20f + skin.atmosphereAlpha * 0.22f).coerceIn(0.20f, 0.46f)
    }

    override fun usesImmersiveForeground(sceneSpec: WeatherSceneSpec, lightContext: WeatherLightContext): Boolean {
        return true
    }

    override fun DrawScope.drawAtmosphere(
        sceneSpec: WeatherSceneSpec,
        lightContext: WeatherLightContext,
        progress: Float,
        immersion: Float,
        skin: ThemeSkin
    ) {
        drawExposureScrim(sceneSpec, lightContext)
        if (lightContext.isNight || sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT) {
            drawRealNightSky(progress, immersion, skin, lightContext)
        } else {
            drawRealSunlight(progress, immersion, skin, lightContext)
        }
        drawCloudLayers(sceneSpec, lightContext, progress, immersion, skin)
        when (sceneSpec.precipitation) {
            WeatherSceneSpec.Precipitation.RAIN -> {
                drawRealRain(progress, immersion, skin, lightContext)
                drawRainTraces(progress, immersion, skin, lightContext)
                drawWetSurfaceSheen(progress, skin, lightContext)
            }
            WeatherSceneSpec.Precipitation.SNOW -> drawRealSnow(progress, immersion, skin, lightContext)
            WeatherSceneSpec.Precipitation.NONE -> Unit
        }
        drawNearHaze(sceneSpec, lightContext, immersion, skin)
    }

    override fun DrawScope.drawHero(
        sceneSpec: WeatherSceneSpec,
        lightContext: WeatherLightContext,
        progress: Float,
        skin: ThemeSkin
    ) {
        drawAtmosphere(sceneSpec, lightContext, progress, 1.0f, skin)
    }
}

private fun DrawScope.drawExposureScrim(
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext
) {
    val night = lightContext.isNight || sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT
    val precipDarkening = if (sceneSpec.precipitation == WeatherSceneSpec.Precipitation.NONE) 0f else 0.12f
    drawRect(
        brush = Brush.verticalGradient(
            listOf(
                Color(0xFF031018).copy(alpha = if (night) 0.62f else 0.16f + precipDarkening),
                Color.Transparent,
                Color(0xFF031018).copy(alpha = if (night) 0.68f else 0.28f + precipDarkening)
            )
        )
    )
}

private fun DrawScope.drawRealSunlight(
    progress: Float,
    immersion: Float,
    skin: ThemeSkin,
    lightContext: WeatherLightContext
) {
    val sunX = (0.16f + lightContext.dayProgress * 0.68f).coerceIn(0.12f, 0.86f)
    val sunY = when (lightContext.phase) {
        WeatherLightContext.Phase.DAWN,
        WeatherLightContext.Phase.DUSK -> 0.20f
        WeatherLightContext.Phase.DAY -> 0.07f + (1f - lightContext.exposure) * 0.10f
        WeatherLightContext.Phase.NIGHT -> 0.10f
    }
    val center = Offset(size.width * (sunX + sin(progress * Math.PI * 2).toFloat() * 0.012f), size.height * sunY)
    val warmthColor = if (lightContext.warmth > 0.70f) Color(0xFFFFB56D) else skin.sunlightColor
    drawRect(
        brush = Brush.radialGradient(
            listOf(
                warmthColor.copy(alpha = (0.20f + lightContext.exposure * 0.22f) * immersion.coerceIn(1f, 1.4f)),
                Color(0xFFFFF3C8).copy(alpha = 0.08f + lightContext.warmth * 0.08f),
                Color.Transparent
            ),
            center = center,
            radius = size.width * 0.72f
        )
    )
    repeat(4) { index ->
        val x = size.width * (sunX - 0.18f + index * 0.11f)
        drawLine(
            color = warmthColor.copy(alpha = (0.030f + index * 0.010f) * lightContext.exposure.coerceAtLeast(0.36f)),
            start = Offset(x, 0f),
            end = Offset(x - size.width * 0.38f, size.height),
            strokeWidth = (42f + index * 16f).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    drawLightBloom(progress, center, warmthColor, lightContext)
}

private fun DrawScope.drawLightBloom(
    progress: Float,
    center: Offset,
    warmthColor: Color,
    lightContext: WeatherLightContext
) {
    val phaseBoost = when (lightContext.phase) {
        WeatherLightContext.Phase.DAWN,
        WeatherLightContext.Phase.DUSK -> 1.28f
        WeatherLightContext.Phase.DAY -> 1.0f
        WeatherLightContext.Phase.NIGHT -> 0f
    }
    if (phaseBoost <= 0f) {
        return
    }
    val beamAlpha = (0.030f + lightContext.exposure * 0.040f) * phaseBoost
    repeat(3) { index ->
        val y = size.height * (0.16f + index * 0.16f)
        drawLine(
            color = warmthColor.copy(alpha = beamAlpha * (1f - index * 0.18f)),
            start = Offset(center.x - size.width * (0.18f + index * 0.10f), y - size.height * 0.28f),
            end = Offset(center.x + size.width * 0.34f, y + size.height * 0.54f),
            strokeWidth = (22f + index * 18f).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    repeat(3) { index ->
        val pulse = 0.85f + sin(progress * 6.28318f + index).coerceIn(-1f, 1f) * 0.10f
        drawCircle(
            color = warmthColor.copy(alpha = beamAlpha * 0.72f / (index + 1)),
            radius = size.width * (0.10f + index * 0.07f) * pulse,
            center = Offset(center.x - size.width * (0.16f + index * 0.10f), center.y + size.height * (0.10f + index * 0.12f))
        )
    }
}

private fun DrawScope.drawRealNightSky(
    progress: Float,
    immersion: Float,
    skin: ThemeSkin,
    lightContext: WeatherLightContext
) {
    val starVisibility = if (lightContext.isNight) 1f else 0.32f
    val starCount = (70 * skin.nightStarDensity * immersion.coerceIn(1f, 1.35f) * starVisibility).toInt()
    for (index in 0 until starCount) {
        val x = size.width * (((index * 47) % 113) / 112f)
        val y = size.height * (((index * 31) % 79) / 100f)
        val twinkle = 0.30f + ((sin(progress * 6.28318f + index * 0.91f) + 1f) * 0.22f)
        val radius = (0.45f + (index % 5) * 0.18f) * skin.nightStarGlowScale
        drawCircle(
            color = skin.nightStarColor.copy(alpha = twinkle * 0.22f * starVisibility),
            radius = (radius * 3.8f).dp.toPx(),
            center = Offset(x, y)
        )
        drawCircle(
            color = skin.nightStarColor.copy(alpha = twinkle * starVisibility),
            radius = radius.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawCloudLayers(
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    progress: Float,
    immersion: Float,
    skin: ThemeSkin
) {
    val categoryWeight = when (sceneSpec.category) {
        WeatherIconUtils.WeatherCategory.SUNNY -> 0.24f
        WeatherIconUtils.WeatherCategory.NIGHT -> 0.18f
        WeatherIconUtils.WeatherCategory.CLOUDY -> 0.56f
        WeatherIconUtils.WeatherCategory.RAIN -> 0.72f
        WeatherIconUtils.WeatherCategory.SNOW -> 0.52f
    }
    val timeWeight = if (lightContext.isNight) 0.78f else 0.88f + lightContext.exposure * 0.18f
    val alpha = (categoryWeight * timeWeight * sceneSpec.atmosphereAlpha * skin.cloudOpacityMultiplier)
        .coerceIn(0.05f, 0.44f)
    val drift = (progress - 0.5f) * size.width * 0.16f * immersion
    drawSoftCloudBand(
        left = size.width * -0.16f + drift,
        top = size.height * 0.18f,
        width = size.width * 0.94f,
        height = size.height * 0.15f,
        alpha = alpha * 0.62f,
        color = skin.cloudColor
    )
    drawSoftCloudBand(
        left = size.width * 0.28f - drift * 0.72f,
        top = size.height * 0.31f,
        width = size.width * 0.98f,
        height = size.height * 0.18f,
        alpha = alpha,
        color = skin.cloudColor
    )
    drawSoftCloudBand(
        left = size.width * -0.22f - drift * 0.38f,
        top = size.height * 0.55f,
        width = size.width * 1.24f,
        height = size.height * 0.24f,
        alpha = alpha * 0.42f,
        color = skin.cloudColor
    )
}

private fun DrawScope.drawSoftCloudBand(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    alpha: Float,
    color: Color
) {
    val cloudColor = color.copy(alpha = alpha.coerceIn(0f, 0.50f))
    drawOval(
        color = cloudColor,
        topLeft = Offset(left, top),
        size = Size(width, height)
    )
    drawOval(
        color = cloudColor.copy(alpha = alpha * 0.72f),
        topLeft = Offset(left + width * 0.18f, top - height * 0.22f),
        size = Size(width * 0.74f, height * 1.10f)
    )
    drawRect(
        brush = Brush.horizontalGradient(
            listOf(Color.Transparent, cloudColor.copy(alpha = alpha * 0.38f), Color.Transparent)
        ),
        topLeft = Offset(left, top + height * 0.36f),
        size = Size(width, height * 0.44f)
    )
}

private fun DrawScope.drawRealRain(
    progress: Float,
    immersion: Float,
    skin: ThemeSkin,
    lightContext: WeatherLightContext
) {
    val count = (112 * immersion.coerceIn(1f, 1.4f)).toInt()
    val nightBoost = if (lightContext.isNight) 1.16f else 1f
    for (index in 0 until count) {
        val column = ((index * 37) % 101) / 100f
        val lane = index / 17
        val x = (size.width * column + lane * size.width * 0.023f) % size.width
        val y = ((progress * 1.35f + index * 0.027f) % 1f) * size.height
        val length = size.height * (0.052f + (index % 4) * 0.012f)
        drawLine(
            color = skin.precipitationColor.copy(alpha = (0.22f + (index % 3) * 0.050f) * skin.precipitationOpacityMultiplier * nightBoost),
            start = Offset(x, y),
            end = Offset(x - size.width * 0.030f, y + length),
            strokeWidth = (0.85f + (index % 3) * 0.20f).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawRainTraces(
    progress: Float,
    immersion: Float,
    skin: ThemeSkin,
    lightContext: WeatherLightContext
) {
    val traceCount = (18 * immersion.coerceIn(1f, 1.4f)).toInt()
    val visibility = if (lightContext.isNight) 1.20f else 0.86f
    for (index in 0 until traceCount) {
        val x = size.width * (((index * 43) % 97) / 96f)
        val y = ((progress * 0.42f + index * 0.071f) % 1f) * size.height
        val length = size.height * (0.10f + (index % 4) * 0.035f)
        val alpha = (0.060f + (index % 3) * 0.024f) * skin.precipitationOpacityMultiplier * visibility
        drawLine(
            color = Color.White.copy(alpha = alpha.coerceIn(0f, 0.24f)),
            start = Offset(x, y),
            end = Offset(x + size.width * 0.018f, y + length),
            strokeWidth = (1.4f + (index % 3) * 0.42f).dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color.White.copy(alpha = (alpha * 1.45f).coerceIn(0f, 0.30f)),
            radius = (1.4f + (index % 4) * 0.28f).dp.toPx(),
            center = Offset(x + size.width * 0.010f, y + length)
        )
    }
}

private fun DrawScope.drawWetSurfaceSheen(
    progress: Float,
    skin: ThemeSkin,
    lightContext: WeatherLightContext
) {
    val coldTint = if (lightContext.isNight) Color(0xFFB8D4FF) else skin.sunlightColor
    drawRect(
        brush = Brush.verticalGradient(
            listOf(
                Color.Transparent,
                coldTint.copy(alpha = if (lightContext.isNight) 0.08f else 0.050f),
                Color.White.copy(alpha = if (lightContext.isNight) 0.055f else 0.034f)
            ),
            startY = size.height * 0.64f,
            endY = size.height
        )
    )
    repeat(5) { index ->
        val phase = progress * 6.28318f + index * 0.92f
        val x = size.width * (0.10f + index * 0.18f + cos(phase) * 0.018f)
        val y = size.height * (0.78f + sin(phase) * 0.018f)
        drawLine(
            color = Color.White.copy(alpha = 0.030f + index * 0.004f),
            start = Offset(x - size.width * 0.16f, y),
            end = Offset(x + size.width * 0.16f, y + size.height * 0.015f),
            strokeWidth = (3.0f + index).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawRealSnow(
    progress: Float,
    immersion: Float,
    skin: ThemeSkin,
    lightContext: WeatherLightContext
) {
    val count = (68 * immersion.coerceIn(1f, 1.35f)).toInt()
    for (index in 0 until count) {
        val baseX = size.width * (((index * 29) % 97) / 96f)
        val sway = sin(progress * 6.28318f + index * 0.64f) * size.width * 0.026f
        val y = ((progress * 0.42f + index * 0.041f) % 1f) * size.height
        val radius = (0.9f + (index % 4) * 0.42f) * skin.particleSizeScale
        drawCircle(
            color = skin.precipitationColor.copy(alpha = ((0.34f + (index % 3) * 0.10f) * if (lightContext.isNight) 1.12f else 1f).coerceIn(0f, 0.82f)),
            radius = radius.dp.toPx(),
            center = Offset((baseX + sway + size.width) % size.width, y)
        )
    }
}

private fun DrawScope.drawNearHaze(
    sceneSpec: WeatherSceneSpec,
    lightContext: WeatherLightContext,
    immersion: Float,
    skin: ThemeSkin
) {
    val phaseHaze = when (lightContext.phase) {
        WeatherLightContext.Phase.DAWN,
        WeatherLightContext.Phase.DUSK -> 1.32f
        WeatherLightContext.Phase.NIGHT -> 0.86f
        WeatherLightContext.Phase.DAY -> 1.0f
    }
    val hazeAlpha = (sceneSpec.hazeOpacity * 0.26f * phaseHaze * immersion).coerceIn(0.03f, 0.22f)
    drawRect(
        brush = Brush.verticalGradient(
            listOf(
                Color.Transparent,
                skin.cloudColor.copy(alpha = hazeAlpha),
                Color(0xFF07171C).copy(alpha = hazeAlpha * 0.78f)
            ),
            startY = size.height * 0.45f,
            endY = size.height
        )
    )
}
