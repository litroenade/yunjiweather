package com.litroenade.yunjiweather.ui.compose.theme.profiles

import androidx.compose.runtime.Immutable
import com.litroenade.yunjiweather.ui.compose.theme.mixins.ThemeMixin
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkin
import com.litroenade.yunjiweather.utils.VisualTheme

@Immutable
data class ThemeProfile(
    val visualTheme: VisualTheme,
    val skin: ThemeSkin,
    val mixins: List<ThemeMixin>
) {
    val key: String
        get() = visualTheme.key
}
