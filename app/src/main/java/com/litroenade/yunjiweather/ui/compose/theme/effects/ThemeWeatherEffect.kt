package com.litroenade.yunjiweather.ui.compose.theme.effects

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.litroenade.yunjiweather.ui.compose.WeatherSceneSpec
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkin

internal interface ThemeWeatherEffect {
    val key: String
    val drawsHeroIcon: Boolean
    val homeBackdropImageResId: Int?
        @DrawableRes get() = null

    fun homeBackdropAlpha(sceneSpec: WeatherSceneSpec): Float = 0f

    fun atmosphereLayerAlpha(skin: ThemeSkin): Float {
        return (0.72f + skin.atmosphereAlpha * 0.28f).coerceIn(0.72f, 1f)
    }

    fun usesImmersiveForeground(sceneSpec: WeatherSceneSpec): Boolean {
        return sceneSpec.usesLightForeground()
    }

    fun DrawScope.drawAtmosphere(
        sceneSpec: WeatherSceneSpec,
        progress: Float,
        immersion: Float,
        skin: ThemeSkin
    )

    fun DrawScope.drawHero(
        sceneSpec: WeatherSceneSpec,
        progress: Float,
        skin: ThemeSkin
    )
}
