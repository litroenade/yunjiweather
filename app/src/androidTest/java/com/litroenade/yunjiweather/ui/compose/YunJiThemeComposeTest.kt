package com.litroenade.yunjiweather.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.litroenade.yunjiweather.data.model.CustomThemeWeatherKey
import com.litroenade.yunjiweather.ui.compose.theme.effects.ThemeWeatherEffectCatalog
import com.litroenade.yunjiweather.ui.compose.screens.CustomThemeEditorPanel
import com.litroenade.yunjiweather.ui.compose.screens.PersonalizationPanel
import com.litroenade.yunjiweather.ui.compose.theme.YunJiTheme
import com.litroenade.yunjiweather.utils.VisualThemeCatalog
import com.litroenade.yunjiweather.utils.VisualThemeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class YunJiThemeComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersContentInsideYunJiTheme() {
        composeRule.setContent {
            YunJiTheme(darkTheme = false) {
                Text("云迹 Compose 验收")
            }
        }

        composeRule.onNodeWithText("云迹 Compose 验收").assertExists()
    }

    @Test
    fun infoCardUsesReadableContentColorInDarkTheme() {
        var contentColor = Color.Unspecified
        var expectedColor = Color.Unspecified

        composeRule.setContent {
            YunJiTheme(darkTheme = true) {
                expectedColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                InfoCard {
                    contentColor = LocalContentColor.current
                    Text("深色卡片文字")
                }
            }
        }

        composeRule.waitForIdle()

        assertEquals(expectedColor, contentColor)
        composeRule.onNodeWithText("深色卡片文字").assertExists()
    }

    @Test
    fun rendersPanoramaWeatherThemeWithoutSeparateStyleLayer() {
        composeRule.setContent {
            YunJiTheme(
                darkTheme = false,
                visualThemeKey = VisualThemeUtils.THEME_PANORAMA
            ) {
                Text("全景天气主题")
            }
        }

        composeRule.onNodeWithText("全景天气主题").assertExists()
    }

    @Test
    fun personalizationPanelShowsHuaweiStyleThemeSections() {
        composeRule.setContent {
            YunJiTheme(
                darkTheme = true,
                visualThemeKey = VisualThemeUtils.THEME_SKY
            ) {
                PersonalizationPanel(
                    themes = VisualThemeCatalog.getThemes(),
                    selectedTheme = VisualThemeUtils.THEME_SKY,
                    onThemeSelected = {}
                )
            }
        }

        composeRule.onAllNodesWithText("龙岗区").assertCountEquals(3)
        composeRule.onNodeWithText("更多皮肤").assertExists()
        composeRule.onAllNodesWithText("默认主题").assertCountEquals(2)
        composeRule.onNodeWithText("全景天气").assertExists()
        assertEquals(VisualThemeUtils.THEME_CUSTOM_1, VisualThemeCatalog.getThemes().last().key)
        assertNull(VisualThemeCatalog.getThemes().firstOrNull { it.key == VisualThemeUtils.THEME_FANTASY })
    }

    @Test
    fun customThemeEditorAppliesDraftOnlyAfterUserConfirms() {
        var appliedImageUris = emptyMap<String, String>()
        var appliedCropAnchors = emptyMap<String, String>()

        composeRule.setContent {
            YunJiTheme(
                darkTheme = true,
                visualThemeKey = VisualThemeUtils.THEME_CUSTOM_1
            ) {
                CustomThemeEditorPanel(
                    customThemeImageUri = "",
                    draftCustomThemeImageUris = mapOf(CustomThemeWeatherKey.FALLBACK to "file:///tmp/yunji-custom-theme.image"),
                    draftCustomThemeCropAnchors = mapOf(CustomThemeWeatherKey.FALLBACK to "bottom"),
                    customThemeEditorMessage = "默认图已导入，确认后点击保存。",
                    onApplyCustomThemeDraft = { imageUris, cropAnchors ->
                        appliedImageUris = imageUris
                        appliedCropAnchors = cropAnchors
                    }
                )
            }
        }

        composeRule.onNodeWithText("待保存").assertExists()
        composeRule.onNodeWithText("保存并应用").performClick()
        composeRule.runOnIdle {
            assertEquals("file:///tmp/yunji-custom-theme.image", appliedImageUris[CustomThemeWeatherKey.FALLBACK])
            assertEquals("bottom", appliedCropAnchors[CustomThemeWeatherKey.FALLBACK])
        }
    }

    @Test
    fun panoramaThemeUsesDedicatedWeatherEffect() {
        val effect = ThemeWeatherEffectCatalog.getEffect(VisualThemeUtils.THEME_PANORAMA)

        assertEquals(VisualThemeUtils.THEME_PANORAMA, effect.key)
        assertFalse(effect.drawsHeroIcon)

        composeRule.setContent {
            YunJiTheme(
                darkTheme = false,
                visualThemeKey = VisualThemeUtils.THEME_PANORAMA
            ) {
                Box(Modifier.size(220.dp)) {
                    WeatherAtmosphere(
                        sceneSpec = WeatherSceneSpec.fromIconCode("100"),
                        immersion = 1.2f,
                        modifier = Modifier.size(220.dp)
                    )
                }
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun weatherAtmosphereHasStableQaTag() {
        composeRule.setContent {
            YunJiTheme(
                darkTheme = false,
                visualThemeKey = VisualThemeUtils.THEME_PANORAMA
            ) {
                Box(Modifier.size(180.dp)) {
                    WeatherAtmosphere(
                        sceneSpec = WeatherSceneSpec.fromIconCode("305"),
                        modifier = Modifier.size(180.dp)
                    )
                }
            }
        }

        composeRule.onNodeWithTag("weather-atmosphere-animation").assertExists()
    }
}
