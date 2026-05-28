package com.litroenade.yunjiweather.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkin
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkinCatalog
import com.litroenade.yunjiweather.ui.compose.theme.skins.official.OfficialWeatherSkin
import com.litroenade.yunjiweather.utils.VisualThemeCatalog
import com.litroenade.yunjiweather.utils.VisualThemeUtils

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
        displayName = "默认主题",
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

val LocalThemeSkin = staticCompositionLocalOf<ThemeSkin> {
    OfficialWeatherSkin.skin
}

@Composable
fun YunJiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    visualThemeKey: String = VisualThemeUtils.THEME_SKY,
    content: @Composable () -> Unit
) {
    val skin = ThemeSkinCatalog.getRuntimeSkin(visualThemeKey)
    val colorScheme = skin.colorScheme(darkTheme)
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = YunJiShapes,
        typography = YunJiTypography,
        content = {
            androidx.compose.runtime.CompositionLocalProvider(
                LocalThemeSkin provides skin,
                LocalYunJiVisualTheme provides visualThemeFor(
                    skin,
                    darkTheme
                )
            ) {
                content()
            }
        }
    )
}

private fun visualThemeFor(
    skin: ThemeSkin,
    darkTheme: Boolean
): YunJiVisualTheme {
    val colorScheme = skin.colorScheme(darkTheme)
    val cardAlpha = if (darkTheme) skin.cardAlphaDark else skin.cardAlphaLight
    val navAlpha = if (darkTheme) skin.navAlphaDark else skin.navAlphaLight
    val cardStroke = if (darkTheme) {
        Color.White.copy(alpha = 0.14f)
    } else {
        colorScheme.onSurface.copy(alpha = 0.10f)
    }
    return YunJiVisualTheme(
        key = skin.key,
        displayName = VisualThemeCatalog.getThemeOrDefault(skin.key).displayName,
        background = colorScheme.background,
        cardContainer = colorScheme.surface.copy(alpha = cardAlpha),
        cardStroke = cardStroke,
        navContainer = colorScheme.surface.copy(alpha = navAlpha),
        navSelectedContainer = colorScheme.secondaryContainer,
        headerAccent = colorScheme.primary,
        primaryWeatherText = colorScheme.onBackground,
        secondaryWeatherText = colorScheme.onSurfaceVariant,
        defaultWeatherGradient = defaultWeatherGradient(skin, darkTheme, colorScheme.background)
    )
}

private fun defaultWeatherGradient(
    skin: ThemeSkin,
    darkTheme: Boolean,
    background: Color
): WeatherGradient {
    return if (darkTheme) {
        WeatherGradient(skin.darkGradientTop, skin.darkGradientMiddle, background)
    } else {
        WeatherGradient(skin.lightGradientTop, skin.lightGradientMiddle, background)
    }
}
