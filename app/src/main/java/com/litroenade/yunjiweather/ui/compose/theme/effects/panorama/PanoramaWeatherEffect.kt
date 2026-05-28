package com.litroenade.yunjiweather.ui.compose.theme.effects.panorama

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.litroenade.yunjiweather.R
import com.litroenade.yunjiweather.ui.compose.WeatherSceneSpec
import com.litroenade.yunjiweather.ui.compose.theme.effects.ThemeWeatherEffect
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkin
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import com.litroenade.yunjiweather.utils.WeatherIconUtils
import kotlin.math.sin

internal object PanoramaWeatherEffect : ThemeWeatherEffect {
    override val key: String = VisualThemeUtils.THEME_PANORAMA
    override val drawsHeroIcon: Boolean = false
    override val homeBackdropImageResId: Int = R.drawable.theme_panorama_preview

    override fun homeBackdropAlpha(sceneSpec: WeatherSceneSpec): Float {
        return if (sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT) 0.36f else 0.66f
    }

    override fun atmosphereLayerAlpha(skin: ThemeSkin): Float {
        return (0.20f + skin.atmosphereAlpha * 0.22f).coerceIn(0.20f, 0.46f)
    }

    override fun usesImmersiveForeground(sceneSpec: WeatherSceneSpec): Boolean {
        return true
    }

    override fun DrawScope.drawAtmosphere(
        sceneSpec: WeatherSceneSpec,
        progress: Float,
        immersion: Float,
        skin: ThemeSkin
    ) {
        drawExposureScrim(sceneSpec)
        if (sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT) {
            drawRealNightSky(progress, immersion, skin)
        } else if (sceneSpec.hasCelestialGlow()) {
            drawRealSunlight(progress, immersion, skin)
        }
        drawCloudLayers(sceneSpec, progress, immersion, skin)
        when (sceneSpec.precipitation) {
            WeatherSceneSpec.Precipitation.RAIN -> drawRealRain(progress, immersion, skin)
            WeatherSceneSpec.Precipitation.SNOW -> drawRealSnow(progress, immersion, skin)
            WeatherSceneSpec.Precipitation.NONE -> Unit
        }
        drawNearHaze(sceneSpec, immersion, skin)
    }

    override fun DrawScope.drawHero(
        sceneSpec: WeatherSceneSpec,
        progress: Float,
        skin: ThemeSkin
    ) {
        drawAtmosphere(sceneSpec, progress, 1.0f, skin)
    }
}

private fun DrawScope.drawExposureScrim(sceneSpec: WeatherSceneSpec) {
    val night = sceneSpec.category == WeatherIconUtils.WeatherCategory.NIGHT
    drawRect(
        brush = Brush.verticalGradient(
            listOf(
                Color(0xFF031018).copy(alpha = if (night) 0.62f else 0.18f),
                Color.Transparent,
                Color(0xFF031018).copy(alpha = if (night) 0.68f else 0.30f)
            )
        )
    )
}

private fun DrawScope.drawRealSunlight(
    progress: Float,
    immersion: Float,
    skin: ThemeSkin
) {
    val center = Offset(size.width * (0.78f + sin(progress * Math.PI * 2).toFloat() * 0.018f), size.height * 0.10f)
    drawRect(
        brush = Brush.radialGradient(
            listOf(
                skin.sunlightColor.copy(alpha = 0.34f * immersion.coerceIn(1f, 1.4f)),
                Color(0xFFFFF3C8).copy(alpha = 0.12f),
                Color.Transparent
            ),
            center = center,
            radius = size.width * 0.72f
        )
    )
    repeat(4) { index ->
        val x = size.width * (0.50f + index * 0.11f)
        drawLine(
            color = skin.sunRayColor.copy(alpha = 0.045f + index * 0.012f),
            start = Offset(x, 0f),
            end = Offset(x - size.width * 0.38f, size.height),
            strokeWidth = (42f + index * 16f).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawRealNightSky(
    progress: Float,
    immersion: Float,
    skin: ThemeSkin
) {
    val starCount = (70 * skin.nightStarDensity * immersion.coerceIn(1f, 1.35f)).toInt()
    for (index in 0 until starCount) {
        val x = size.width * (((index * 47) % 113) / 112f)
        val y = size.height * (((index * 31) % 79) / 100f)
        val twinkle = 0.30f + ((sin(progress * 6.28318f + index * 0.91f) + 1f) * 0.22f)
        val radius = (0.45f + (index % 5) * 0.18f) * skin.nightStarGlowScale
        drawCircle(
            color = skin.nightStarColor.copy(alpha = twinkle * 0.22f),
            radius = (radius * 3.8f).dp.toPx(),
            center = Offset(x, y)
        )
        drawCircle(
            color = skin.nightStarColor.copy(alpha = twinkle),
            radius = radius.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawCloudLayers(
    sceneSpec: WeatherSceneSpec,
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
    val alpha = (categoryWeight * sceneSpec.atmosphereAlpha * skin.cloudOpacityMultiplier)
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
    skin: ThemeSkin
) {
    val count = (96 * immersion.coerceIn(1f, 1.4f)).toInt()
    for (index in 0 until count) {
        val column = ((index * 37) % 101) / 100f
        val lane = index / 17
        val x = (size.width * column + lane * size.width * 0.023f) % size.width
        val y = ((progress * 1.35f + index * 0.027f) % 1f) * size.height
        val length = size.height * (0.052f + (index % 4) * 0.012f)
        drawLine(
            color = skin.precipitationColor.copy(alpha = (0.18f + (index % 3) * 0.045f) * skin.precipitationOpacityMultiplier),
            start = Offset(x, y),
            end = Offset(x - size.width * 0.030f, y + length),
            strokeWidth = (0.85f + (index % 3) * 0.20f).dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawRealSnow(
    progress: Float,
    immersion: Float,
    skin: ThemeSkin
) {
    val count = (68 * immersion.coerceIn(1f, 1.35f)).toInt()
    for (index in 0 until count) {
        val baseX = size.width * (((index * 29) % 97) / 96f)
        val sway = sin(progress * 6.28318f + index * 0.64f) * size.width * 0.026f
        val y = ((progress * 0.42f + index * 0.041f) % 1f) * size.height
        val radius = (0.9f + (index % 4) * 0.42f) * skin.particleSizeScale
        drawCircle(
            color = skin.precipitationColor.copy(alpha = (0.34f + (index % 3) * 0.10f).coerceIn(0f, 0.76f)),
            radius = radius.dp.toPx(),
            center = Offset((baseX + sway + size.width) % size.width, y)
        )
    }
}

private fun DrawScope.drawNearHaze(
    sceneSpec: WeatherSceneSpec,
    immersion: Float,
    skin: ThemeSkin
) {
    val hazeAlpha = (sceneSpec.hazeOpacity * 0.26f * immersion).coerceIn(0.03f, 0.18f)
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
