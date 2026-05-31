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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.litroenade.yunjiweather.data.model.CustomThemeAsset
import com.litroenade.yunjiweather.data.model.CustomThemeProfile
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

private val YunJiTypography = Typography(
    headlineSmall = TextStyle(
        fontSize = 23.sp,
        lineHeight = 29.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontSize = 17.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = TextStyle(
        fontSize = 15.sp,
        lineHeight = 21.sp,
        fontWeight = FontWeight.Medium
    ),
    bodyLarge = TextStyle(
        fontSize = 15.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Normal
    ),
    labelLarge = TextStyle(
        fontSize = 13.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontSize = 10.sp,
        lineHeight = 13.sp,
        fontWeight = FontWeight.Medium
    )
)

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

val LocalThemeSkin = staticCompositionLocalOf {
    OfficialWeatherSkin.skin
}

@Composable
fun YunJiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    visualThemeKey: String = VisualThemeUtils.THEME_SKY,
    customThemeImageUri: String = "",
    customThemeCropAnchor: String = "center",
    customThemeImageUris: Map<String, String> = emptyMap(),
    customThemeCropAnchors: Map<String, String> = emptyMap(),
    customThemeProfile: CustomThemeProfile = CustomThemeProfile.empty(),
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
                ),
                LocalCustomThemeOptions provides CustomThemeOptions(
                    imageUri = customThemeImageUri,
                    cropAnchor = customThemeCropAnchor,
                    imagesByWeatherKey = customThemeImageUris.mapValues { entry ->
                        CustomThemeImage(
                            uri = entry.value,
                            cropAnchor = customThemeCropAnchors[entry.key] ?: customThemeCropAnchor,
                            mediaType = mediaTypeFromUri(entry.value)
                        )
                    },
                    profile = customThemeProfile
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

private fun mediaTypeFromUri(uri: String): String {
    return if (uri.trim().lowercase().endsWith(".gif")) CustomThemeAsset.MEDIA_GIF else CustomThemeAsset.MEDIA_IMAGE
}
