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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.litroenade.yunjiweather.ui.home.WeatherAnimationType
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
internal fun WeatherAnimation(
    iconCode: String,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "weather-animation")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weather-animation-progress"
    )
    val animationType = WeatherAnimationType.fromIconCode(iconCode)

    Canvas(modifier = modifier) {
        if (size.width <= 0f || size.height <= 0f) {
            return@Canvas
        }
        when (animationType) {
            WeatherAnimationType.SUNNY -> drawSunny(progress)
            WeatherAnimationType.NIGHT -> drawNight(progress)
            WeatherAnimationType.RAIN -> {
                drawCloudy(progress)
                drawRain(progress)
            }

            WeatherAnimationType.SNOW -> {
                drawCloudy(progress)
                drawSnow(progress)
            }

            WeatherAnimationType.CLOUDY -> drawCloudy(progress)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSunny(progress: Float) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = min(size.width, size.height) * 0.23f
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
            color = Color(0xFFFFB840),
            start = start,
            end = end,
            strokeWidth = 5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    drawCircle(color = Color(0xFFFFCD52), radius = radius, center = center)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNight(progress: Float) {
    val radius = min(size.width, size.height) * 0.24f
    val center = Offset(size.width * 0.48f, size.height * 0.46f)
    drawPath(
        path = crescentPath(center, radius),
        color = Color(0xFFF4E7D0)
    )
    for (index in 0 until 4) {
        val starX = size.width * (0.22f + index * 0.15f)
        val starY = size.height * (0.24f + ((progress + index * 0.27f) % 1f) * 0.18f)
        drawCircle(
            color = Color(0xFFFFF4E3),
            radius = 2.4.dp.toPx() + (index % 2),
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloudy(progress: Float) {
    val offset = (progress - 0.5f) * size.width * 0.08f
    val cloudColor = Color(0xFFF7FAF8)
    val shadowColor = Color(0x330B2430)
    drawCloudShape(offset + size.width * 0.025f, size.height * 0.035f, shadowColor)
    drawCloudShape(offset, 0f, cloudColor)
    drawCircle(
        color = Color.White.copy(alpha = 0.45f),
        radius = size.width * 0.12f,
        center = Offset(size.width * 0.46f + offset, size.height * 0.38f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloudShape(
    offsetX: Float,
    offsetY: Float,
    cloudColor: Color
) {
    drawCircle(
        color = cloudColor,
        radius = size.width * 0.18f,
        center = Offset(size.width * 0.38f + offsetX, size.height * 0.55f + offsetY)
    )
    drawCircle(
        color = cloudColor,
        radius = size.width * 0.23f,
        center = Offset(size.width * 0.52f + offsetX, size.height * 0.45f + offsetY)
    )
    drawCircle(
        color = cloudColor,
        radius = size.width * 0.16f,
        center = Offset(size.width * 0.68f + offsetX, size.height * 0.56f + offsetY)
    )
    drawRoundRect(
        color = cloudColor,
        topLeft = Offset(size.width * 0.25f + offsetX, size.height * 0.53f + offsetY),
        size = Size(size.width * 0.55f, size.height * 0.19f),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRain(progress: Float) {
    for (index in 0 until 5) {
        val baseX = size.width * (0.28f + index * 0.12f)
        val y = size.height * 0.72f + ((progress + index * 0.18f) % 1f) * size.height * 0.20f
        drawLine(
            color = Color(0xFF78906F),
            start = Offset(baseX, y),
            end = Offset(baseX - size.width * 0.04f, y + size.height * 0.10f),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSnow(progress: Float) {
    for (index in 0 until 6) {
        val x = size.width * (0.25f + index * 0.10f)
        val y = size.height * 0.70f + ((progress + index * 0.14f) % 1f) * size.height * 0.22f
        drawSnowflake(Offset(x, y), 4.dp.toPx())
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSnowflake(center: Offset, radius: Float) {
    drawCircle(color = Color.White, radius = radius, center = center)
    drawCircle(
        color = Color(0xFFE5EEF8),
        radius = radius * 1.7f,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
}
