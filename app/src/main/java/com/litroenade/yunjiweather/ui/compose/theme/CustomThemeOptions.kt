package com.litroenade.yunjiweather.ui.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor
import com.litroenade.yunjiweather.data.model.CustomThemeWeatherKey
import com.litroenade.yunjiweather.ui.compose.WeatherLightContext
import com.litroenade.yunjiweather.ui.compose.WeatherSceneSpec

@Immutable
data class CustomThemeImage(
    val uri: String = "",
    val cropAnchor: String = CustomThemeCropAnchor.CENTER
)

@Immutable
data class CustomThemeOptions(
    val imageUri: String = "",
    val cropAnchor: String = CustomThemeCropAnchor.CENTER,
    val imagesByWeatherKey: Map<String, CustomThemeImage> = emptyMap()
) {
    fun imageForScene(sceneSpec: WeatherSceneSpec, lightContext: WeatherLightContext): CustomThemeImage {
        val weatherKey = if (lightContext.isNight) {
            CustomThemeWeatherKey.NIGHT
        } else {
            CustomThemeWeatherKey.fromWeatherCategory(sceneSpec.category)
        }
        return imagesByWeatherKey[weatherKey]
            ?: imagesByWeatherKey[CustomThemeWeatherKey.FALLBACK]
            ?: CustomThemeImage(imageUri, cropAnchor)
    }
}

val LocalCustomThemeOptions = staticCompositionLocalOf { CustomThemeOptions() }

fun customThemeCropAlignment(cropAnchor: String): Alignment {
    return when (cropAnchor) {
        CustomThemeCropAnchor.TOP -> Alignment.TopCenter
        CustomThemeCropAnchor.BOTTOM -> Alignment.BottomCenter
        else -> Alignment.Center
    }
}
