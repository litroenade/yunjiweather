package com.litroenade.yunjiweather.ui.compose.theme.skins

import com.litroenade.yunjiweather.ui.compose.theme.profiles.ThemeProfileCatalog

object ThemeSkinCatalog {
    @JvmStatic
    fun getSkin(themeKey: String): ThemeSkin {
        return ThemeProfileCatalog.getProfile(themeKey).skin
    }

    @JvmStatic
    fun getRuntimeSkin(themeKey: String): ThemeSkin {
        return ThemeProfileCatalog.getRuntimeProfile(themeKey).skin
    }

    @JvmStatic
    fun getThemeFolder(themeKey: String): String {
        return ThemeProfileCatalog.getThemeFolder(themeKey)
    }
}
