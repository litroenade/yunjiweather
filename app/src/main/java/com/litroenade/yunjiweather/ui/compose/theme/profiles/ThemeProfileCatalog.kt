package com.litroenade.yunjiweather.ui.compose.theme.profiles

import com.litroenade.yunjiweather.ui.compose.theme.mixins.ThemeMixinCatalog
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkin
import com.litroenade.yunjiweather.ui.compose.theme.skins.custom.CustomPlaceholderSkin
import com.litroenade.yunjiweather.ui.compose.theme.skins.fantasy.FantasyPlaceholderSkin
import com.litroenade.yunjiweather.ui.compose.theme.skins.official.OfficialWeatherSkin
import com.litroenade.yunjiweather.ui.compose.theme.skins.panorama.PanoramaWeatherSkin
import com.litroenade.yunjiweather.utils.VisualThemeCatalog
import com.litroenade.yunjiweather.utils.VisualThemeUtils

object ThemeProfileCatalog {
    private val profiles = listOf(
        profile(VisualThemeUtils.THEME_SKY, OfficialWeatherSkin.skin),
        profile(VisualThemeUtils.THEME_PANORAMA, PanoramaWeatherSkin.skin),
        profile(VisualThemeUtils.THEME_FANTASY, FantasyPlaceholderSkin.skin),
        profile(VisualThemeUtils.THEME_CUSTOM_1, CustomPlaceholderSkin.skin)
    )
    private val profilesByKey = profiles.associateBy { profile -> profile.key }
    private val defaultProfile = profilesByKey.getValue(VisualThemeUtils.THEME_SKY)

    @JvmStatic
    fun getProfiles(): List<ThemeProfile> {
        return profiles
    }

    @JvmStatic
    fun getProfile(themeKey: String): ThemeProfile {
        val catalogThemeKey = VisualThemeCatalog.getThemeOrDefault(themeKey).key
        return profilesByKey[catalogThemeKey] ?: defaultProfile
    }

    @JvmStatic
    fun getRuntimeProfile(themeKey: String): ThemeProfile {
        val normalizedThemeKey = VisualThemeUtils.normalizeThemeKey(themeKey)
        val profile = getProfile(normalizedThemeKey)
        return if (profile.skin.runtimeSelectable) profile else defaultProfile
    }

    @JvmStatic
    fun getThemeFolder(themeKey: String): String {
        return getProfile(themeKey).skin.folderName
    }

    private fun profile(themeKey: String, skin: ThemeSkin): ThemeProfile {
        return ThemeProfile(
            visualTheme = VisualThemeCatalog.getThemeOrDefault(themeKey),
            skin = skin,
            mixins = ThemeMixinCatalog.getMixins(themeKey)
        )
    }
}
