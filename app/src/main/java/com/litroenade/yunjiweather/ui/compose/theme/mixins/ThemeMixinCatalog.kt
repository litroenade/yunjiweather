package com.litroenade.yunjiweather.ui.compose.theme.mixins

import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleDefinition
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleKeys
import com.litroenade.yunjiweather.utils.VisualThemeUtils

object ThemeMixinCatalog {
    private val panoramaCalendarModule = HomeModuleDefinition(
        key = HomeModuleKeys.CALENDAR_LIFE,
        displayName = "日历生活",
        shortDescription = "日期、农历和生活建议入口",
        sourceThemeKey = VisualThemeUtils.THEME_PANORAMA
    )

    private val mixinsByTheme = mapOf(
        VisualThemeUtils.THEME_PANORAMA to listOf(
            ThemeMixin(
                key = "panorama_calendar_life",
                homeModules = listOf(panoramaCalendarModule)
            )
        )
    )

    @JvmStatic
    fun getMixins(themeKey: String): List<ThemeMixin> {
        return mixinsByTheme[VisualThemeUtils.normalizeThemeKey(themeKey)].orEmpty()
    }

    @JvmStatic
    fun getHomeModules(themeKey: String): List<HomeModuleDefinition> {
        return getMixins(themeKey).flatMap { mixin -> mixin.homeModules }
    }

    @JvmStatic
    fun getAllHomeModules(): List<HomeModuleDefinition> {
        return mixinsByTheme.values.flatten().flatMap { mixin -> mixin.homeModules }
    }
}

