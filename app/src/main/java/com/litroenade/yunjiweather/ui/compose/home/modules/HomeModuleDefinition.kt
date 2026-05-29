package com.litroenade.yunjiweather.ui.compose.home.modules

import androidx.compose.runtime.Immutable

@Immutable
data class HomeModuleDefinition(
    val key: String,
    val displayName: String,
    val shortDescription: String,
    val sourceThemeKey: String? = null,
    val defaultEnabled: Boolean = true
)
