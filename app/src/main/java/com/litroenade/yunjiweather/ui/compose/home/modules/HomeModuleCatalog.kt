package com.litroenade.yunjiweather.ui.compose.home.modules

import com.litroenade.yunjiweather.ui.compose.theme.mixins.ThemeMixinCatalog
import com.litroenade.yunjiweather.utils.HomeBlock

object HomeModuleCatalog {
    const val MODULE_CALENDAR_LIFE = HomeModuleKeys.CALENDAR_LIFE

    private val builtInDefinitions = HomeBlock.defaultOrder().map { block ->
        HomeModuleDefinition(
            key = block.key,
            displayName = block.displayName,
            shortDescription = block.shortDescription
        )
    }

    @JvmStatic
    fun getBuiltInModules(): List<HomeModuleDefinition> {
        return builtInDefinitions
    }

    @JvmStatic
    fun getAvailableModules(themeKey: String): List<HomeModuleDefinition> {
        return mergeDefinitions(builtInDefinitions + ThemeMixinCatalog.getHomeModules(themeKey))
    }

    @JvmStatic
    fun getAvailableModuleKeys(themeKey: String): List<String> {
        return getAvailableModules(themeKey).map { definition -> definition.key }
    }

    @JvmStatic
    fun getDefinition(moduleKey: String): HomeModuleDefinition {
        return allKnownDefinitions().firstOrNull { definition -> definition.key == moduleKey }
            ?: HomeModuleDefinition(moduleKey, moduleKey, "主题扩展模块")
    }

    private fun allKnownDefinitions(): List<HomeModuleDefinition> {
        return mergeDefinitions(builtInDefinitions + ThemeMixinCatalog.getAllHomeModules())
    }

    private fun mergeDefinitions(definitions: List<HomeModuleDefinition>): List<HomeModuleDefinition> {
        return definitions.distinctBy { definition -> definition.key }
    }
}
