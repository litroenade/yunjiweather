package com.litroenade.yunjiweather.ui.compose.theme.mixins

import androidx.compose.runtime.Immutable
import com.litroenade.yunjiweather.ui.compose.home.modules.HomeModuleDefinition

@Immutable
data class ThemeMixin(
    val key: String,
    val homeModules: List<HomeModuleDefinition> = emptyList()
)
