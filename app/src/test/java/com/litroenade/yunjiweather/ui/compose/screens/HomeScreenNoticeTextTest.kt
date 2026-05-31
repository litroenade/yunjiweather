package com.litroenade.yunjiweather.ui.compose.screens

import com.litroenade.yunjiweather.common.UiState
import com.litroenade.yunjiweather.data.model.HomeWeatherData
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeScreenNoticeTextTest {

    @Test
    fun cacheRefreshingNoticeShowsOnlyWhileRefreshingCachedWeather() {
        val notice = "\u5df2\u663e\u793a\u8fc7\u671f\u672c\u5730\u7f13\u5b58\uff0c\u6b63\u5728\u5237\u65b0\u5929\u6c14\u3002"
        val cacheState = UiState.cache(homeWeather(), notice, 100L)
        val successState = UiState.success(homeWeather())

        assertEquals(notice, homeHeaderNoticeText(notice, true, cacheState))
        assertEquals("", homeHeaderNoticeText(notice, false, cacheState))
        assertEquals("", homeHeaderNoticeText(notice, true, successState))
    }

    @Test
    fun normalStatusMessageIsNotTreatedAsPersistentCacheRefreshNotice() {
        val state = UiState.success(homeWeather())
        val notice = "\u5df2\u5b9a\u4f4d\u5230 \u9752\u5c9b\u5e02"

        assertEquals(notice, homeHeaderNoticeText(" $notice ", false, state))
    }

    private fun homeWeather(): HomeWeatherData {
        return HomeWeatherData(
            "\u9752\u5c9b\u5e02",
            "qingdao",
            "21",
            "\u9634",
            "1",
            "25",
            "20",
            "65",
            "\u4e1c\u5317\u98ce",
            "2",
            "12",
            "1010",
            "10",
            "80",
            1_700_000_000_000L,
            "\u5916\u5957",
            "\u9002\u5b9c",
            "45",
            "\u826f",
            "PM2.5"
        )
    }
}
