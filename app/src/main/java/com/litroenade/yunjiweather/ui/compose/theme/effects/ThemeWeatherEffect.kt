package com.litroenade.yunjiweather.ui.compose.theme.effects

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.litroenade.yunjiweather.ui.compose.WeatherLightContext
import com.litroenade.yunjiweather.ui.compose.WeatherSceneSpec
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkin

internal interface ThemeWeatherEffect {
    val key: String
    val drawsHeroIcon: Boolean

    @DrawableRes
    fun homeBackdropImageResId(sceneSpec: WeatherSceneSpec, lightContext: WeatherLightContext): Int?

    fun homeBackdropAlpha(sceneSpec: WeatherSceneSpec, lightContext: WeatherLightContext): Float = 0f

    fun atmosphereLayerAlpha(skin: ThemeSkin): Float {
        return (0.72f + skin.atmosphereAlpha * 0.28f).coerceIn(0.72f, 1f)
    }

    fun usesImmersiveForeground(sceneSpec: WeatherSceneSpec, lightContext: WeatherLightContext): Boolean {
        return sceneSpec.usesLightForeground() || lightContext.isNight
    }

    fun DrawScope.drawAtmosphere(
        sceneSpec: WeatherSceneSpec,
        lightContext: WeatherLightContext,
        progress: Float,
        immersion: Float,
        skin: ThemeSkin
    )

    fun DrawScope.drawHero(
        sceneSpec: WeatherSceneSpec,
        lightContext: WeatherLightContext,
        progress: Float,
        skin: ThemeSkin
    )
}
