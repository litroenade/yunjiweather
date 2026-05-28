package com.litroenade.yunjiweather.ui.compose.theme.skins

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class ThemeSkin(
    val key: String,
    val folderName: String,
    val previewTitle: String,
    val previewSubtitle: String,
    val runtimeSelectable: Boolean,
    val homeImmersion: Float,
    val heroAnimationScale: Float,
    val atmosphereAlpha: Float,
    val weatherAnimationSpeed: Float,
    val sunGlowScale: Float,
    val sunDiscScale: Float,
    val cloudShapeScale: Float,
    val cloudOpacityMultiplier: Float,
    val precipitationOpacityMultiplier: Float,
    val particleSizeScale: Float,
    val sunlightColor: Color,
    val sunRayColor: Color,
    val cloudColor: Color,
    val precipitationColor: Color,
    val nightGradientTop: Color,
    val nightGradientMiddle: Color,
    val nightGradientBottom: Color,
    val nightStarColor: Color,
    val nightStarDensity: Float,
    val nightStarGlowScale: Float,
    val cardAlphaLight: Float,
    val cardAlphaDark: Float,
    val navAlphaLight: Float,
    val navAlphaDark: Float,
    val previewTop: Color,
    val previewMiddle: Color,
    val previewBottom: Color,
    val lightGradientTop: Color,
    val lightGradientMiddle: Color,
    val darkGradientTop: Color,
    val darkGradientMiddle: Color,
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme
) {
    fun colorScheme(darkTheme: Boolean): ColorScheme {
        return if (darkTheme) darkColorScheme else lightColorScheme
    }

    fun isRuntimeSelectable(): Boolean {
        return runtimeSelectable
    }
}
