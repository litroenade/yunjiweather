package com.litroenade.yunjiweather.ui.compose.theme.skins

import com.litroenade.yunjiweather.ui.compose.theme.skins.custom.CustomPlaceholderSkin
import com.litroenade.yunjiweather.ui.compose.theme.skins.fantasy.FantasyPlaceholderSkin
import com.litroenade.yunjiweather.ui.compose.theme.skins.official.OfficialWeatherSkin
import com.litroenade.yunjiweather.ui.compose.theme.skins.panorama.PanoramaWeatherSkin
import com.litroenade.yunjiweather.utils.VisualThemeUtils

object ThemeSkinCatalog {
    private val skins = listOf(
        OfficialWeatherSkin.skin,
        PanoramaWeatherSkin.skin,
        FantasyPlaceholderSkin.skin,
        CustomPlaceholderSkin.skin
    )
    private val skinsByKey = skins.associateBy { skin -> skin.key }

    @JvmStatic
    fun getSkin(themeKey: String): ThemeSkin {
        return skinsByKey[themeKey] ?: OfficialWeatherSkin.skin
    }

    @JvmStatic
    fun getRuntimeSkin(themeKey: String): ThemeSkin {
        val normalizedKey = VisualThemeUtils.normalizeThemeKey(themeKey)
        val skin = getSkin(normalizedKey)
        return if (skin.runtimeSelectable) skin else OfficialWeatherSkin.skin
    }

    @JvmStatic
    fun getThemeFolder(themeKey: String): String {
        return getSkin(themeKey).folderName
    }
}
