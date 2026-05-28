package com.litroenade.yunjiweather.ui.compose.theme.effects.official

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.litroenade.yunjiweather.ui.compose.WeatherSceneSpec
import com.litroenade.yunjiweather.ui.compose.theme.effects.ThemeWeatherEffect
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkin
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import com.litroenade.yunjiweather.utils.WeatherIconUtils
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

internal object OfficialWeatherEffect : ThemeWeatherEffect {
    override val key: String = VisualThemeUtils.THEME_SKY
    override val drawsHeroIcon: Boolean = true

    override fun DrawScope.drawAtmosphere(
        sceneSpec: WeatherSceneSpec,
        progress: Float,
        immersion: Float,
        skin: ThemeSkin
    ) {
        drawHaze(sceneSpec, immersion, skin)
        if (sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT) {
            drawNightStars(progress, immersion, skin)
        }
        drawAtmosphereClouds(sceneSpec, progress, immersion, skin)
        if (sceneSpec.hasCelestialGlow()) {
            drawCelestialGlow(sceneSpec, progress, immersion, skin)
        }
        when (sceneSpec.precipitation) {
            WeatherSceneSpec.Precipitation.RAIN -> drawAtmosphereRain(sceneSpec, progress, immersion, skin)
            WeatherSceneSpec.Precipitation.SNOW -> drawAtmosphereSnow(sceneSpec, progress, immersion, skin)
            WeatherSceneSpec.Precipitation.NONE -> Unit
        }
    }

    override fun DrawScope.drawHero(
        sceneSpec: WeatherSceneSpec,
        progress: Float,
        skin: ThemeSkin
    ) {
        when (sceneSpec.category) {
            WeatherIconUtils.WeatherCategory.SUNNY -> drawSunny(progress, skin)
            WeatherIconUtils.WeatherCategory.NIGHT -> drawNight(progress, skin)
            WeatherIconUtils.WeatherCategory.RAIN -> {
                drawCloudy(progress, skin)
                drawRain(progress, skin)
            }
            WeatherIconUtils.WeatherCategory.SNOW -> {
                drawCloudy(progress, skin)
                drawSnow(progress, skin)
            }
            WeatherIconUtils.WeatherCategory.CLOUDY -> drawCloudy(progress, skin)
        }
    }
}

private fun DrawScope.drawNightStars(
    progress: Float,
    immersion: Float,
    skin: ThemeSkin
) {
    val starCount = (42 * skin.nightStarDensity * immersion.coerceIn(1f, 1.45f)).toInt().coerceAtLeast(18)
    for (index in 0 until starCount) {
        val xFactor = ((index * 37) % 101) / 100f
        val yFactor = ((index * 53) % 89) / 100f
        val drift = sin((progress * 360f + index * 19f).toRadians()).toFloat() * size.width * 0.006f
        val twinkle = (0.42f + ((sin((progress * 360f + index * 41f).toRadians()).toFloat() + 1f) * 0.24f))
            .coerceIn(0.28f, 0.90f)
        val radius = ((0.7f + (index % 4) * 0.32f) * skin.nightStarGlowScale).dp.toPx()
        val center = Offset((size.width * xFactor + drift + size.width) % size.width, size.height * yFactor)
        drawCircle(
            color = skin.nightStarColor.copy(alpha = twinkle * 0.34f),
            radius = radius * 2.8f,
            center = center
        )
        drawCircle(
            color = skin.nightStarColor.copy(alpha = twinkle),
            radius = radius,
            center = center
        )
    }
}

private fun DrawScope.drawHaze(
    sceneSpec: WeatherSceneSpec,
    immersion: Float,
    skin: ThemeSkin
) {
    val haze = (sceneSpec.hazeOpacity * immersion).coerceIn(0f, 1f)
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                skin.sunlightColor.copy(alpha = (haze * 0.18f * skin.sunGlowScale).coerceIn(0f, 0.42f)),
                skin.cloudColor.copy(alpha = (haze * 0.30f * skin.cloudOpacityMultiplier).coerceIn(0f, 0.46f)),
                Color.Transparent
            ),
            center = Offset(size.width * 0.44f, size.height * 0.18f),
            radius = size.width * 0.82f
        )
    )
    drawRect(
        color = skin.cloudColor.copy(alpha = (haze * 0.05f * skin.cloudOpacityMultiplier).coerceIn(0f, 0.10f)),
        topLeft = Offset(0f, size.height * 0.54f),
        size = Size(size.width, size.height * 0.46f)
    )
}

private fun DrawScope.drawCelestialGlow(
    sceneSpec: WeatherSceneSpec,
    progress: Float,
    immersion: Float,
    skin: ThemeSkin
) {
    val center = if (sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT) {
        Offset(size.width * 0.78f, size.height * 0.12f)
    } else {
        Offset(size.width * (0.70f + progress * 0.04f), size.height * 0.13f)
    }
    val baseColor = skin.sunlightColor
    drawCircle(
        color = baseColor.copy(alpha = (sceneSpec.atmosphereAlpha * immersion * skin.sunGlowScale * 0.34f).coerceIn(0f, 0.78f)),
        radius = size.minDimension * (0.42f + (immersion - 1f).coerceIn(0f, 0.5f) * 0.10f) * skin.sunGlowScale,
        center = center
    )
    drawCircle(
        color = baseColor.copy(alpha = (sceneSpec.atmosphereAlpha * immersion).coerceIn(0f, 1f)),
        radius = size.minDimension * 0.12f * immersion.coerceIn(1f, 1.32f) * skin.sunDiscScale,
        center = center
    )
}

private fun DrawScope.drawAtmosphereClouds(
    sceneSpec: WeatherSceneSpec,
    progress: Float,
    immersion: Float,
    skin: ThemeSkin
) {
    val cloudAlpha = (sceneSpec.cloudOpacity * sceneSpec.atmosphereAlpha * immersion * skin.cloudOpacityMultiplier)
        .coerceIn(0f, 1f)
    if (cloudAlpha <= 0f) {
        return
    }
    val drift = (progress - 0.5f) * size.width * 0.20f * immersion
    drawWideCloud(
        offsetX = size.width * 0.14f + drift,
        offsetY = size.height * 0.20f,
        scale = 1.12f * immersion.coerceIn(1f, 1.22f) * skin.cloudShapeScale,
        alpha = cloudAlpha,
        baseColor = skin.cloudColor
    )
    drawWideCloud(
        offsetX = size.width * 0.60f - drift * 0.72f,
        offsetY = size.height * 0.34f,
        scale = 0.86f * immersion.coerceIn(1f, 1.18f) * skin.cloudShapeScale,
        alpha = cloudAlpha * 0.86f,
        baseColor = skin.cloudColor
    )
    drawWideCloud(
        offsetX = size.width * -0.04f - drift * 0.48f,
        offsetY = size.height * 0.46f,
        scale = 0.78f * immersion.coerceIn(1f, 1.14f) * skin.cloudShapeScale,
        alpha = cloudAlpha * 0.62f,
        baseColor = skin.cloudColor
    )
}

private fun DrawScope.drawWideCloud(
    offsetX: Float,
    offsetY: Float,
    scale: Float,
    alpha: Float,
    baseColor: Color
) {
    val cloudColor = baseColor.copy(alpha = alpha.coerceIn(0f, 0.48f))
    val width = size.width * 0.46f * scale
    val height = size.height * 0.09f * scale
    drawCircle(cloudColor, width * 0.24f, Offset(offsetX + width * 0.25f, offsetY + height * 0.62f))
    drawCircle(cloudColor, width * 0.30f, Offset(offsetX + width * 0.48f, offsetY + height * 0.40f))
    drawCircle(cloudColor, width * 0.22f, Offset(offsetX + width * 0.70f, offsetY + height * 0.66f))
    drawRoundRect(
        color = cloudColor,
        topLeft = Offset(offsetX + width * 0.10f, offsetY + height * 0.52f),
        size = Size(width * 0.78f, height * 0.62f),
        cornerRadius = CornerRadius(height, height)
    )
}

private fun DrawScope.drawAtmosphereRain(
    sceneSpec: WeatherSceneSpec,
    progress: Float,
    immersion: Float,
    skin: ThemeSkin
) {
    val particleCount = (sceneSpec.particleCount * immersion).toInt().coerceAtLeast(sceneSpec.particleCount)
    for (index in 0 until particleCount) {
        val column = (index % 13) / 12f
        val row = index / 13
        val x = size.width * (column + 0.04f * (row % 2)) % size.width
        val y = ((progress + index * 0.037f) % 1f) * size.height
        drawLine(
            color = skin.precipitationColor.copy(alpha = (0.36f * skin.precipitationOpacityMultiplier).coerceIn(0f, 0.72f)),
            start = Offset(x, y),
            end = Offset(x - size.width * 0.035f, y + size.height * 0.075f),
            strokeWidth = (1.3f * immersion.coerceIn(1f, 1.28f) * skin.particleSizeScale).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawAtmosphereSnow(
    sceneSpec: WeatherSceneSpec,
    progress: Float,
    immersion: Float,
    skin: ThemeSkin
) {
    val particleCount = (sceneSpec.particleCount * immersion).toInt().coerceAtLeast(sceneSpec.particleCount)
    for (index in 0 until particleCount) {
        val column = (index % 11) / 10f
        val row = index / 11
        val sway = sin((progress * 360f + index * 27f).toRadians()).toFloat() * size.width * 0.018f * immersion
        val x = size.width * column + sway + row * size.width * 0.015f
        val y = ((progress * 0.62f + index * 0.043f) % 1f) * size.height
        drawCircle(
            color = skin.precipitationColor.copy(alpha = (0.58f * skin.precipitationOpacityMultiplier).coerceIn(0f, 0.82f)),
            radius = ((1.6f + (index % 3) * 0.7f * immersion.coerceIn(1f, 1.25f)) * skin.particleSizeScale).dp.toPx(),
            center = Offset(x % size.width, y)
        )
    }
}

private fun DrawScope.drawSunny(
    progress: Float,
    skin: ThemeSkin
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = min(size.width, size.height) * 0.23f * skin.sunDiscScale
    drawCircle(
        color = skin.sunlightColor.copy(alpha = 0.24f),
        radius = radius * 2.1f * skin.sunGlowScale,
        center = center
    )
    for (index in 0 until 12) {
        val angle = Math.toRadians((index * 30f + progress * 360f).toDouble())
        val start = Offset(
            center.x + cos(angle).toFloat() * radius * 1.35f,
            center.y + sin(angle).toFloat() * radius * 1.35f
        )
        val end = Offset(
            center.x + cos(angle).toFloat() * radius * 1.85f,
            center.y + sin(angle).toFloat() * radius * 1.85f
        )
        drawLine(
            color = skin.sunRayColor,
            start = start,
            end = end,
            strokeWidth = (5f * skin.sunDiscScale).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    drawCircle(color = skin.sunlightColor, radius = radius, center = center)
}

private fun DrawScope.drawNight(
    progress: Float,
    skin: ThemeSkin
) {
    val radius = min(size.width, size.height) * 0.24f * skin.sunDiscScale
    val center = Offset(size.width * 0.48f, size.height * 0.46f)
    drawPath(
        path = crescentPath(center, radius),
        color = skin.sunlightColor.copy(alpha = 0.92f)
    )
    val starCount = (4 * skin.nightStarDensity).toInt().coerceAtLeast(4)
    for (index in 0 until starCount) {
        val starX = size.width * ((0.22f + index * 0.15f) % 1f)
        val starY = size.height * (0.24f + ((progress + index * 0.27f) % 1f) * 0.18f)
        drawCircle(
            color = skin.nightStarColor.copy(alpha = 0.88f),
            radius = (2.4f * skin.nightStarGlowScale).dp.toPx() + (index % 2),
            center = Offset(starX, starY)
        )
    }
}

private fun crescentPath(center: Offset, radius: Float): Path {
    val cutoutCenter = Offset(center.x + radius * 0.42f, center.y - radius * 0.18f)
    return Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(Rect(center = center, radius = radius))
        addOval(Rect(center = cutoutCenter, radius = radius * 0.88f))
    }
}

private fun DrawScope.drawCloudy(
    progress: Float,
    skin: ThemeSkin
) {
    val offset = (progress - 0.5f) * size.width * 0.08f * skin.weatherAnimationSpeed
    val cloudColor = skin.cloudColor
    val shadowColor = Color(0x330B2430)
    drawCloudShape(offset + size.width * 0.025f, size.height * 0.035f, shadowColor, skin.cloudShapeScale)
    drawCloudShape(offset, 0f, cloudColor, skin.cloudShapeScale)
    drawCircle(
        color = skin.cloudColor.copy(alpha = (0.45f * skin.cloudOpacityMultiplier).coerceIn(0f, 0.74f)),
        radius = size.width * 0.12f * skin.cloudShapeScale,
        center = Offset(size.width * 0.46f + offset, size.height * 0.38f)
    )
}

private fun DrawScope.drawCloudShape(
    offsetX: Float,
    offsetY: Float,
    cloudColor: Color,
    scale: Float
) {
    drawCircle(
        color = cloudColor,
        radius = size.width * 0.18f * scale,
        center = Offset(size.width * 0.38f + offsetX, size.height * 0.55f + offsetY)
    )
    drawCircle(
        color = cloudColor,
        radius = size.width * 0.23f * scale,
        center = Offset(size.width * 0.52f + offsetX, size.height * 0.45f + offsetY)
    )
    drawCircle(
        color = cloudColor,
        radius = size.width * 0.16f * scale,
        center = Offset(size.width * 0.68f + offsetX, size.height * 0.56f + offsetY)
    )
    drawRoundRect(
        color = cloudColor,
        topLeft = Offset(size.width * 0.25f + offsetX, size.height * 0.53f + offsetY),
        size = Size(size.width * 0.55f * scale, size.height * 0.19f * scale),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
    )
}

private fun DrawScope.drawRain(
    progress: Float,
    skin: ThemeSkin
) {
    for (index in 0 until 5) {
        val baseX = size.width * (0.28f + index * 0.12f)
        val y = size.height * 0.72f + ((progress + index * 0.18f) % 1f) * size.height * 0.20f
        drawLine(
            color = skin.precipitationColor.copy(alpha = (0.78f * skin.precipitationOpacityMultiplier).coerceIn(0f, 1f)),
            start = Offset(baseX, y),
            end = Offset(baseX - size.width * 0.04f, y + size.height * 0.10f),
            strokeWidth = (4f * skin.particleSizeScale).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSnow(
    progress: Float,
    skin: ThemeSkin
) {
    for (index in 0 until 6) {
        val x = size.width * (0.25f + index * 0.10f)
        val y = size.height * 0.70f + ((progress + index * 0.14f) % 1f) * size.height * 0.22f
        drawSnowflake(Offset(x, y), (4f * skin.particleSizeScale).dp.toPx(), skin)
    }
}

private fun DrawScope.drawSnowflake(
    center: Offset,
    radius: Float,
    skin: ThemeSkin
) {
    drawCircle(color = skin.precipitationColor, radius = radius, center = center)
    drawCircle(
        color = skin.precipitationColor.copy(alpha = 0.56f),
        radius = radius * 1.7f,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun Float.toRadians(): Double {
    return Math.toRadians(toDouble())
}
