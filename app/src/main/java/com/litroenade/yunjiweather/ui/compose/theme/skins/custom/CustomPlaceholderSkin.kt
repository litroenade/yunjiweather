package com.litroenade.yunjiweather.ui.compose.theme.skins.custom

import androidx.compose.ui.graphics.Color
import com.litroenade.yunjiweather.ui.compose.theme.skins.ThemeSkin
import com.litroenade.yunjiweather.ui.compose.theme.skins.official.OfficialWeatherSkin
import com.litroenade.yunjiweather.utils.VisualThemeUtils

object CustomPlaceholderSkin {
    val skin = OfficialWeatherSkin.skin.copy(
        key = VisualThemeUtils.THEME_CUSTOM_1,
        folderName = "custom",
        previewTitle = "自定义主题",
        previewSubtitle = "本地图片",
        runtimeSelectable = true,
        homeImmersion = 0.82f,
        heroAnimationScale = 0.9f,
        atmosphereAlpha = 0.52f,
        previewTop = Color(0xFF2E3440),
        previewMiddle = Color(0xFF4C566A),
        previewBottom = Color(0xFF88C0D0)
    )
}
