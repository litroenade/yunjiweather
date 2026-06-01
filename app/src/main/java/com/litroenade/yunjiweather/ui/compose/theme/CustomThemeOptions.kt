package com.litroenade.yunjiweather.ui.compose.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import com.litroenade.yunjiweather.data.model.CustomThemeAsset
import com.litroenade.yunjiweather.data.model.CustomThemeCropAnchor
import com.litroenade.yunjiweather.data.model.CustomThemeProfile
import com.litroenade.yunjiweather.data.model.CustomThemeResolver
import com.litroenade.yunjiweather.data.model.CustomThemeWeatherKey
import com.litroenade.yunjiweather.ui.compose.WeatherLightContext
import com.litroenade.yunjiweather.ui.compose.WeatherSceneSpec

@Immutable
data class CustomThemeImage(
    val assetId: String = "",
    val uri: String = "",
    val cropAnchor: String = CustomThemeCropAnchor.CENTER,
    val mediaType: String = CustomThemeAsset.MEDIA_IMAGE
)

@Immutable
data class CustomThemeOptions(
    val imageUri: String = "",
    val cropAnchor: String = CustomThemeCropAnchor.CENTER,
    val imagesByWeatherKey: Map<String, CustomThemeImage> = emptyMap(),
    val profile: CustomThemeProfile = CustomThemeProfile.empty()
) {
    fun imageForScene(
        sceneSpec: WeatherSceneSpec,
        lightContext: WeatherLightContext,
        minuteOfDay: Int = -1
    ): CustomThemeImage {
        val weatherKey = CustomThemeWeatherKey.fromWeatherCategory(sceneSpec.category)
        if (!profile.isEmpty) {
            val asset = runCatching {
                CustomThemeResolver.resolve(profile, weatherKey, lightContext.isNight, minuteOfDay)
            }.getOrDefault(CustomThemeAsset.empty())
            if (!asset.isEmpty) {
                return CustomThemeImage(
                    assetId = asset.id,
                    uri = asset.uri,
                    cropAnchor = asset.cropAnchor,
                    mediaType = asset.mediaType
                )
            }
        }
        val phaseKeys = when (lightContext.phase) {
            WeatherLightContext.Phase.DAWN -> CustomThemeWeatherKey.DAWN
            WeatherLightContext.Phase.DUSK -> CustomThemeWeatherKey.DUSK
            else -> null
        }
        val nightCombinationKey = when {
            !lightContext.isNight -> null
            weatherKey == CustomThemeWeatherKey.RAIN -> CustomThemeWeatherKey.RAIN_NIGHT
            weatherKey == CustomThemeWeatherKey.SNOW -> CustomThemeWeatherKey.SNOW_NIGHT
            else -> CustomThemeWeatherKey.NIGHT
        }
        return firstConfiguredImage(phaseKeys, nightCombinationKey, if (lightContext.isNight) CustomThemeWeatherKey.NIGHT else null)
            ?: imagesByWeatherKey[weatherKey]
            ?: imagesByWeatherKey[CustomThemeWeatherKey.FALLBACK]
            ?: CustomThemeImage(uri = imageUri, cropAnchor = cropAnchor)
    }

    private fun firstConfiguredImage(vararg keys: String?): CustomThemeImage? {
        for (key in keys) {
            if (!key.isNullOrBlank()) {
                val image = imagesByWeatherKey[key]
                if (image != null) {
                    return image
                }
            }
        }
        return null
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
