package com.litroenade.yunjiweather.ui.compose

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.litroenade.yunjiweather.ui.compose.theme.YunJiTheme
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
}
