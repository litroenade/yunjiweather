package com.litroenade.yunjiweather.ui.compose

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.litroenade.yunjiweather.ui.compose.theme.YunJiTheme
import org.junit.Assert.assertEquals
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
}
