package com.litroenade.yunjiweather.ui.compose.theme.skins.fantasy

import com.litroenade.yunjiweather.ui.compose.theme.skins.panorama.PanoramaWeatherSkin
import com.litroenade.yunjiweather.utils.VisualThemeUtils

object FantasyPlaceholderSkin {
    val skin = PanoramaWeatherSkin.skin.copy(
        key = VisualThemeUtils.THEME_FANTASY,
        folderName = "fantasy",
        previewTitle = "幻想乡",
        previewSubtitle = "预留皮肤",
        runtimeSelectable = false,
        homeImmersion = 0f,
        heroAnimationScale = 0f,
        atmosphereAlpha = 0f
    )
}
