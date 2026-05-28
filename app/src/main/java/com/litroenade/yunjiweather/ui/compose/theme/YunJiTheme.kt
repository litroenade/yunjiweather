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
import com.litroenade.yunjiweather.utils.VisualThemeCatalog
import com.litroenade.yunjiweather.utils.VisualThemeStyleCatalog
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

private val SoftMistLightColors = lightColorScheme(
    primary = Color(0xFF5F7F8A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCECEF),
    onPrimaryContainer = Color(0xFF243D43),
    secondary = Color(0xFF687A62),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE7EFE1),
    onSecondaryContainer = Color(0xFF2A3727),
    tertiary = Color(0xFF8A7359),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF0E5D9),
    onTertiaryContainer = Color(0xFF3A2D22),
    background = Color(0xFFF6F8F8),
    onBackground = Color(0xFF253033),
    surface = Color(0xFFFFFEFC),
    onSurface = Color(0xFF253033),
    surfaceVariant = Color(0xFFE5EAEB),
    onSurfaceVariant = Color(0xFF606C70),
    error = Color(0xFFB42318)
)

private val SoftMistDarkColors = darkColorScheme(
    primary = Color(0xFFADCED6),
    onPrimary = Color(0xFF203A40),
    primaryContainer = Color(0xFF38545B),
    onPrimaryContainer = Color(0xFFD9F1F5),
    secondary = Color(0xFFC1D4B8),
    onSecondary = Color(0xFF2B3927),
    secondaryContainer = Color(0xFF44533E),
    onSecondaryContainer = Color(0xFFE6F4DF),
    tertiary = Color(0xFFE1C8AD),
    onTertiary = Color(0xFF3D2E20),
    tertiaryContainer = Color(0xFF584735),
    onTertiaryContainer = Color(0xFFFFE7CF),
    background = Color(0xFF171F22),
    onBackground = Color(0xFFE7ECEE),
    surface = Color(0xFF202A2D),
    onSurface = Color(0xFFE7ECEE),
    surfaceVariant = Color(0xFF374246),
    onSurfaceVariant = Color(0xFFCAD4D8),
    error = Color(0xFFFFB4AB)
)

private val SunsetLightColors = lightColorScheme(
    primary = Color(0xFFB8643A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDDCC),
    onPrimaryContainer = Color(0xFF4A1F0E),
    secondary = Color(0xFF82652E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF2E4C5),
    onSecondaryContainer = Color(0xFF3A2E13),
    tertiary = Color(0xFF9A5871),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF8D8E4),
    onTertiaryContainer = Color(0xFF3F1B29),
    background = Color(0xFFFFF6EF),
    onBackground = Color(0xFF332824),
    surface = Color(0xFFFFFBF8),
    onSurface = Color(0xFF332824),
    surfaceVariant = Color(0xFFF2E1D8),
    onSurfaceVariant = Color(0xFF6A5A51),
    error = Color(0xFFB42318)
)

private val SunsetDarkColors = darkColorScheme(
    primary = Color(0xFFFFB68E),
    onPrimary = Color(0xFF4A1F0E),
    primaryContainer = Color(0xFF7B3F22),
    onPrimaryContainer = Color(0xFFFFDDCC),
    secondary = Color(0xFFE8CB83),
    onSecondary = Color(0xFF3D2E08),
    secondaryContainer = Color(0xFF5A4514),
    onSecondaryContainer = Color(0xFFFFE9AF),
    tertiary = Color(0xFFE9AFC4),
    onTertiary = Color(0xFF421B2B),
    tertiaryContainer = Color(0xFF6B374C),
    onTertiaryContainer = Color(0xFFFFD8E6),
    background = Color(0xFF251B18),
    onBackground = Color(0xFFF7E7DE),
    surface = Color(0xFF30231F),
    onSurface = Color(0xFFF7E7DE),
    surfaceVariant = Color(0xFF4A3932),
    onSurfaceVariant = Color(0xFFDCC7BB),
    error = Color(0xFFFFB4AB)
)

private val DeepSeaLightColors = lightColorScheme(
    primary = Color(0xFF286D79),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCDEEF3),
    onPrimaryContainer = Color(0xFF08363D),
    secondary = Color(0xFF405F7A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCEBFA),
    onSecondaryContainer = Color(0xFF162F45),
    tertiary = Color(0xFF625B97),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE6E1FF),
    onTertiaryContainer = Color(0xFF28234B),
    background = Color(0xFFEFF7FA),
    onBackground = Color(0xFF17282E),
    surface = Color(0xFFF9FEFF),
    onSurface = Color(0xFF17282E),
    surfaceVariant = Color(0xFFD8E9EF),
    onSurfaceVariant = Color(0xFF4E6570),
    error = Color(0xFFB42318)
)

private val DeepSeaDarkColors = darkColorScheme(
    primary = Color(0xFF87D7E6),
    onPrimary = Color(0xFF05353F),
    primaryContainer = Color(0xFF155562),
    onPrimaryContainer = Color(0xFFC8F4FA),
    secondary = Color(0xFFA7CBE8),
    onSecondary = Color(0xFF173449),
    secondaryContainer = Color(0xFF2C4D67),
    onSecondaryContainer = Color(0xFFD7EBFA),
    tertiary = Color(0xFFC9C2FF),
    onTertiary = Color(0xFF302A5E),
    tertiaryContainer = Color(0xFF48417A),
    onTertiaryContainer = Color(0xFFE7E1FF),
    background = Color(0xFF0F1B22),
    onBackground = Color(0xFFE5F1F5),
    surface = Color(0xFF16242C),
    onSurface = Color(0xFFE5F1F5),
    surfaceVariant = Color(0xFF2D424D),
    onSurfaceVariant = Color(0xFFC5D8DF),
    error = Color(0xFFFFB4AB)
)

private val YunJiShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp)
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
        cardContainer = Color.White.copy(alpha = 0.30f),
        cardStroke = Color.White.copy(alpha = 0.28f),
        navContainer = Color.White.copy(alpha = 0.68f),
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
    visualThemeStyleKey: String = VisualThemeStyleCatalog.STYLE_DEFAULT,
    content: @Composable () -> Unit
) {
    val normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(visualThemeKey)
    val normalizedStyleKey = VisualThemeStyleCatalog.getStyleOrDefault(visualThemeStyleKey).key
    val colorScheme = colorSchemeFor(normalizedThemeKey, normalizedStyleKey, darkTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = YunJiShapes,
        typography = YunJiTypography,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalYunJiVisualTheme provides visualThemeFor(
                    normalizedThemeKey,
                    normalizedStyleKey,
                    colorScheme,
                    darkTheme
                )
            ) {
                content()
            }
        }
    )
}

private fun colorSchemeFor(themeKey: String, styleKey: String, darkTheme: Boolean): ColorScheme {
    return when (styleKey) {
        VisualThemeStyleCatalog.STYLE_SOFT_MIST -> if (darkTheme) SoftMistDarkColors else SoftMistLightColors
        VisualThemeStyleCatalog.STYLE_SUNSET -> if (darkTheme) SunsetDarkColors else SunsetLightColors
        VisualThemeStyleCatalog.STYLE_DEEP_SEA -> if (darkTheme) DeepSeaDarkColors else DeepSeaLightColors
        else -> when (themeKey) {
            VisualThemeUtils.THEME_SAKURA -> if (darkTheme) SakuraDarkColors else SakuraLightColors
            VisualThemeUtils.THEME_FANTASY -> if (darkTheme) FantasyDarkColors else FantasyLightColors
            else -> if (darkTheme) DarkColors else LightColors
        }
    }
}

private fun visualThemeFor(
    themeKey: String,
    styleKey: String,
    colorScheme: ColorScheme,
    darkTheme: Boolean
): YunJiVisualTheme {
    val cardAlpha = if (darkTheme) 0.42f else 0.30f
    val navAlpha = if (darkTheme) 0.70f else 0.68f
    val cardStroke = if (darkTheme) {
        Color.White.copy(alpha = 0.14f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.10f)
    }
    return when (themeKey) {
        VisualThemeUtils.THEME_SAKURA -> YunJiVisualTheme(
            key = themeKey,
            displayName = VisualThemeCatalog.getThemeOrDefault(themeKey).displayName,
            background = colorScheme.background,
            cardContainer = colorScheme.surface.copy(alpha = cardAlpha),
            cardStroke = cardStroke,
            navContainer = colorScheme.surface.copy(alpha = navAlpha),
            navSelectedContainer = colorScheme.secondaryContainer,
            headerAccent = colorScheme.primary,
            primaryWeatherText = colorScheme.onBackground,
            secondaryWeatherText = colorScheme.onSurfaceVariant,
            defaultWeatherGradient = defaultWeatherGradient(themeKey, styleKey, darkTheme, colorScheme)
        )

        VisualThemeUtils.THEME_FANTASY -> YunJiVisualTheme(
            key = themeKey,
            displayName = VisualThemeCatalog.getThemeOrDefault(themeKey).displayName,
            background = colorScheme.background,
            cardContainer = colorScheme.surface.copy(alpha = cardAlpha),
            cardStroke = cardStroke,
            navContainer = colorScheme.surface.copy(alpha = navAlpha),
            navSelectedContainer = colorScheme.secondaryContainer,
            headerAccent = colorScheme.primary,
            primaryWeatherText = colorScheme.onBackground,
            secondaryWeatherText = colorScheme.onSurfaceVariant,
            defaultWeatherGradient = defaultWeatherGradient(themeKey, styleKey, darkTheme, colorScheme)
        )

        else -> YunJiVisualTheme(
            key = themeKey,
            displayName = VisualThemeCatalog.getThemeOrDefault(themeKey).displayName,
            background = colorScheme.background,
            cardContainer = colorScheme.surface.copy(alpha = cardAlpha),
            cardStroke = cardStroke,
            navContainer = colorScheme.surface.copy(alpha = navAlpha),
            navSelectedContainer = colorScheme.secondaryContainer,
            headerAccent = colorScheme.primary,
            primaryWeatherText = colorScheme.onBackground,
            secondaryWeatherText = colorScheme.onSurfaceVariant,
            defaultWeatherGradient = defaultWeatherGradient(themeKey, styleKey, darkTheme, colorScheme)
        )
    }
}

private fun defaultWeatherGradient(
    themeKey: String,
    styleKey: String,
    darkTheme: Boolean,
    colorScheme: ColorScheme
): WeatherGradient {
    return when (styleKey) {
        VisualThemeStyleCatalog.STYLE_SOFT_MIST -> if (darkTheme) {
            WeatherGradient(Color(0xFF1D2A2F), Color(0xFF203136), colorScheme.background)
        } else {
            WeatherGradient(Color(0xFFD8E8ED), Color(0xFFF0F5F6), colorScheme.background)
        }

        VisualThemeStyleCatalog.STYLE_SUNSET -> if (darkTheme) {
            WeatherGradient(Color(0xFF3B241D), Color(0xFF2D2021), colorScheme.background)
        } else {
            WeatherGradient(Color(0xFFFFC7A6), Color(0xFFFFE7D7), colorScheme.background)
        }

        VisualThemeStyleCatalog.STYLE_DEEP_SEA -> if (darkTheme) {
            WeatherGradient(Color(0xFF0F3441), Color(0xFF102933), colorScheme.background)
        } else {
            WeatherGradient(Color(0xFF8CCFE0), Color(0xFFD5EEF5), colorScheme.background)
        }

        else -> when (themeKey) {
            VisualThemeUtils.THEME_SAKURA -> if (darkTheme) {
                WeatherGradient(Color(0xFF31212A), Color(0xFF2A2228), colorScheme.background)
            } else {
                WeatherGradient(Color(0xFFF3C8D6), Color(0xFFF7E7EC), colorScheme.background)
            }

            VisualThemeUtils.THEME_FANTASY -> if (darkTheme) {
                WeatherGradient(Color(0xFF26343A), Color(0xFF2C2D31), colorScheme.background)
            } else {
                WeatherGradient(Color(0xFFD6E7E9), Color(0xFFF0E4D0), colorScheme.background)
            }

            else -> if (darkTheme) {
                WeatherGradient(Color(0xFF172C3A), Color(0xFF18272C), colorScheme.background)
            } else {
                WeatherGradient(Color(0xFFA9D8F5), Color(0xFFD9EEF3), colorScheme.background)
            }
        }
    }
}
