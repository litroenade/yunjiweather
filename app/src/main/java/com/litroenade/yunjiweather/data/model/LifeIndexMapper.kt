package com.litroenade.yunjiweather.data.model

import com.litroenade.yunjiweather.data.api.model.QWeatherIndicesResponse
import java.io.IOException

object LifeIndexMapper {

    @JvmStatic
    @Throws(IOException::class)
    fun mapDailyIndices(dailyList: List<QWeatherIndicesResponse.Daily>?): List<LifeIndexItem> {
        if (dailyList.isNullOrEmpty()) {
            throw IOException("生活指数接口缺少 daily 数据")
        }
        val result = mutableListOf<LifeIndexItem>()
        dailyList.forEach { daily ->
            val advice = requireText(daily.text, "indices.daily.text")
            result += LifeIndexItem(
                requireText(daily.name, "indices.daily.name"),
                requireText(daily.category, "indices.daily.category"),
                advice,
                advice
            )
        }
        if (result.isEmpty()) {
            throw IOException("生活指数接口没有可展示数据")
        }
        return result
    }

    @Throws(IOException::class)
    private fun requireText(value: String?, fieldName: String): String {
        if (value.isNullOrBlank()) {
            throw IOException("生活指数接口缺少字段：$fieldName")
        }
        return value
    }
}
