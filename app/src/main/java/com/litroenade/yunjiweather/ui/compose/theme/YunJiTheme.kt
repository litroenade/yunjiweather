package com.litroenade.yunjiweather.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.litroenade.yunjiweather.utils.VisualThemeUtils

private val LightColors = lightColorScheme(
    primary = Color(0xFF2F7D89),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9F1F4),
    onPrimaryContainer = Color(0xFF113B42),
    secondary = Color(0xFF4D6F38),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE5F1DA),
    onSecondaryContainer = Color(0xFF20351A),
    tertiary = Color(0xFF9B6A2C),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE6C6),
    onTertiaryContainer = Color(0xFF3F2606),
    background = Color(0xFFF4F7F8),
    onBackground = Color(0xFF172326),
    surface = Color.White,
    onSurface = Color(0xFF172326),
    surfaceVariant = Color(0xFFE1EAEC),
    onSurfaceVariant = Color(0xFF506267),
    error = Color(0xFFB42318)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FD4DF),
    onPrimary = Color(0xFF06343B),
    primaryContainer = Color(0xFF194F58),
    onPrimaryContainer = Color(0xFFC8F4FA),
    secondary = Color(0xFFB7D89A),
    onSecondary = Color(0xFF1D320D),
    secondaryContainer = Color(0xFF334C22),
    onSecondaryContainer = Color(0xFFE0F4CF),
    tertiary = Color(0xFFE8C28A),
    onTertiary = Color(0xFF3B2506),
    tertiaryContainer = Color(0xFF5A3B12),
    onTertiaryContainer = Color(0xFFFFE2B5),
    background = Color(0xFF111A1D),
    onBackground = Color(0xFFE8EFF1),
    surface = Color(0xFF192427),
    onSurface = Color(0xFFE8EFF1),
    surfaceVariant = Color(0xFF314044),
    onSurfaceVariant = Color(0xFFC9D6D9),
    error = Color(0xFFFFB4AB)
)

private val SakuraLightColors = lightColorScheme(
    primary = Color(0xFFB65A76),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF7D9E3),
    onPrimaryContainer = Color(0xFF481927),
    secondary = Color(0xFF2F7D75),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F1EF),
    onSecondaryContainer = Color(0xFF143B38),
    tertiary = Color(0xFFC27A2C),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE5C6),
    onTertiaryContainer = Color(0xFF4A2B05),
    background = Color(0xFFF7F1F4),
    onBackground = Color(0xFF2F2930),
    surface = Color(0xFFFFF9FB),
    onSurface = Color(0xFF2F2930),
    surfaceVariant = Color(0xFFF0DCE4),
    onSurfaceVariant = Color(0xFF6F5962),
    error = Color(0xFFB42318)
)

private val SakuraDarkColors = darkColorScheme(
    primary = Color(0xFFF2A9BE),
    onPrimary = Color(0xFF481927),
    primaryContainer = Color(0xFF74344A),
    onPrimaryContainer = Color(0xFFFFD9E4),
    secondary = Color(0xFF8DD8CE),
    onSecondary = Color(0xFF123834),
    secondaryContainer = Color(0xFF1F514C),
    onSecondaryContainer = Color(0xFFC7F2EC),
    tertiary = Color(0xFFF0C06E),
    onTertiary = Color(0xFF3D2E08),
    tertiaryContainer = Color(0xFF5A421C),
    onTertiaryContainer = Color(0xFFFFE2B0),
    background = Color(0xFF231D22),
    onBackground = Color(0xFFF5E7ED),
    surface = Color(0xFF30272E),
    onSurface = Color(0xFFF5E7ED),
    surfaceVariant = Color(0xFF4B3B45),
    onSurfaceVariant = Color(0xFFE0C7D1),
    error = Color(0xFFFFB4AB)
)

private val FantasyLightColors = lightColorScheme(
    primary = Color(0xFF386F78),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E7E9),
    onPrimaryContainer = Color(0xFF123438),
    secondary = Color(0xFF866B31),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEAE2D1),
    onSecondaryContainer = Color(0xFF3B311B),
    tertiary = Color(0xFF5D659D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE3E6FF),
    onTertiaryContainer = Color(0xFF242B58),
    background = Color(0xFFF4F0E8),
    onBackground = Color(0xFF292B2F),
    surface = Color(0xFFFFFBF4),
    onSurface = Color(0xFF292B2F),
    surfaceVariant = Color(0xFFE5DED0),
    onSurfaceVariant = Color(0xFF5F5A51),
    error = Color(0xFFB42318)
)

private val FantasyDarkColors = darkColorScheme(
    primary = Color(0xFF92CCD2),
    onPrimary = Color(0xFF123438),
    primaryContainer = Color(0xFF2F5358),
    onPrimaryContainer = Color(0xFFD0F1F4),
    secondary = Color(0xFFE5C574),
    onSecondary = Color(0xFF3D2E08),
    secondaryContainer = Color(0xFF59481E),
    onSecondaryContainer = Color(0xFFFFE6A7),
    tertiary = Color(0xFFB9C1FF),
    onTertiary = Color(0xFF252B5A),
    tertiaryContainer = Color(0xFF3B426F),
    onTertiaryContainer = Color(0xFFE0E4FF),
    background = Color(0xFF232529),
    onBackground = Color(0xFFEAE6DD),
    surface = Color(0xFF303136),
    onSurface = Color(0xFFEAE6DD),
    surfaceVariant = Color(0xFF494840),
    onSurfaceVariant = Color(0xFFD5CFC3),
    error = Color(0xFFFFB4AB)
)

private val YunJiShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

private val YunJiTypography = Typography()

@Immutable
data class WeatherGradient(
    val top: Color,
    val middle: Color,
    val bottom: Color
)

@Immutable
data class YunJiVisualTheme(
    val key: String,
    val displayName: String,
    val background: Color,
    val cardContainer: Color,
    val cardStroke: Color,
    val navContainer: Color,
    val navSelectedContainer: Color,
    val headerAccent: Color,
    val primaryWeatherText: Color,
    val secondaryWeatherText: Color,
    val defaultWeatherGradient: WeatherGradient
)

val LocalYunJiVisualTheme = staticCompositionLocalOf {
    YunJiVisualTheme(
        key = VisualThemeUtils.THEME_SKY,
        displayName = "经典晴空",
        background = Color(0xFFF4F7F8),
        cardContainer = Color.White.copy(alpha = 0.88f),
        cardStroke = Color.White.copy(alpha = 0.58f),
        navContainer = Color.White.copy(alpha = 0.90f),
        navSelectedContainer = Color(0xFFE5F1DA),
        headerAccent = Color(0xFF2F7D89),
        primaryWeatherText = Color(0xFF12343A),
        secondaryWeatherText = Color(0xFF496A72),
        defaultWeatherGradient = WeatherGradient(Color(0xFFA9D8F5), Color(0xFFD9EEF3), Color(0xFFF4F7F8))
    )
}

@Composable
fun YunJiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    visualThemeKey: String = VisualThemeUtils.THEME_SKY,
    content: @Composable () -> Unit
) {
    val normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(visualThemeKey)
    val colorScheme = colorSchemeFor(normalizedThemeKey, darkTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = YunJiShapes,
        typography = YunJiTypography,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalYunJiVisualTheme provides visualThemeFor(normalizedThemeKey, colorScheme, darkTheme)
            ) {
                content()
            }
        }
    )
}

private fun colorSchemeFor(themeKey: String, darkTheme: Boolean): ColorScheme {
    return when (themeKey) {
        VisualThemeUtils.THEME_SAKURA -> if (darkTheme) SakuraDarkColors else SakuraLightColors
        VisualThemeUtils.THEME_FANTASY -> if (darkTheme) FantasyDarkColors else FantasyLightColors
        else -> if (darkTheme) DarkColors else LightColors
    }
}

private fun visualThemeFor(themeKey: String, colorScheme: ColorScheme, darkTheme: Boolean): YunJiVisualTheme {
    val cardAlpha = if (darkTheme) 0.78f else 0.88f
    val navAlpha = if (darkTheme) 0.82f else 0.90f
    return when (themeKey) {
        VisualThemeUtils.THEME_SAKURA -> YunJiVisualTheme(
            key = themeKey,
            displayName = "樱雨粉",
            background = colorScheme.background,
            cardContainer = colorScheme.surface.copy(alpha = cardAlpha),
            cardStroke = colorScheme.surfaceVariant.copy(alpha = 0.55f),
            navContainer = colorScheme.surface.copy(alpha = navAlpha),
            navSelectedContainer = colorScheme.secondaryContainer,
            headerAccent = colorScheme.primary,
            primaryWeatherText = colorScheme.onBackground,
            secondaryWeatherText = colorScheme.onSurfaceVariant,
            defaultWeatherGradient = WeatherGradient(Color(0xFFF3C8D6), Color(0xFFF7E7EC), colorScheme.background)
        )

        VisualThemeUtils.THEME_FANTASY -> YunJiVisualTheme(
            key = themeKey,
            displayName = "幻想天",
            background = colorScheme.background,
            cardContainer = colorScheme.surface.copy(alpha = cardAlpha),
            cardStroke = colorScheme.surfaceVariant.copy(alpha = 0.58f),
            navContainer = colorScheme.surface.copy(alpha = navAlpha),
            navSelectedContainer = colorScheme.secondaryContainer,
            headerAccent = colorScheme.primary,
            primaryWeatherText = colorScheme.onBackground,
            secondaryWeatherText = colorScheme.onSurfaceVariant,
            defaultWeatherGradient = WeatherGradient(Color(0xFFD6E7E9), Color(0xFFF0E4D0), colorScheme.background)
        )

        else -> YunJiVisualTheme(
            key = VisualThemeUtils.THEME_SKY,
            displayName = "经典晴空",
            background = colorScheme.background,
            cardContainer = colorScheme.surface.copy(alpha = cardAlpha),
            cardStroke = colorScheme.surfaceVariant.copy(alpha = 0.55f),
            navContainer = colorScheme.surface.copy(alpha = navAlpha),
            navSelectedContainer = colorScheme.secondaryContainer,
            headerAccent = colorScheme.primary,
            primaryWeatherText = colorScheme.onBackground,
            secondaryWeatherText = colorScheme.onSurfaceVariant,
            defaultWeatherGradient = WeatherGradient(Color(0xFFA9D8F5), Color(0xFFD9EEF3), colorScheme.background)
        )
    }
}
