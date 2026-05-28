package com.litroenade.yunjiweather.ui.compose.theme.effects

import com.litroenade.yunjiweather.ui.compose.theme.effects.official.OfficialWeatherEffect
import com.litroenade.yunjiweather.ui.compose.theme.effects.panorama.PanoramaWeatherEffect
import com.litroenade.yunjiweather.utils.VisualThemeUtils

internal object ThemeWeatherEffectCatalog {
    private val effects = listOf(
        OfficialWeatherEffect,
        PanoramaWeatherEffect
    ).associateBy { effect -> effect.key }

    fun getEffect(themeKey: String): ThemeWeatherEffect {
        return effects[VisualThemeUtils.normalizeThemeKey(themeKey)] ?: OfficialWeatherEffect
    }
}
