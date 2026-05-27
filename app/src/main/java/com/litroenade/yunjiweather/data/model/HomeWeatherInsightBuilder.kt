package com.litroenade.yunjiweather.data.model

import com.litroenade.yunjiweather.data.entity.WarningEntity
import com.litroenade.yunjiweather.utils.AirQualityUtils
import com.litroenade.yunjiweather.utils.WindScaleUtils

object HomeWeatherInsightBuilder {

    @JvmStatic
    fun build(data: HomeWeatherData, warnings: List<WarningEntity>): HomeWeatherInsight {
        if (warnings.isNotEmpty()) {
            val warning = warnings.first()
            return HomeWeatherInsight(
                primaryTitle = "天气预警",
                primarySubtitle = "${warning.title} · ${warning.level}",
                isPrimaryOpensAlerts = true,
                secondaryTitle = "本地建议",
                secondarySubtitle = "${data.condition}，空气${data.airQualityCategory}，${data.travelAdvice}"
            )
        }
        val condition = data.condition
        if (condition.contains("雨") || condition.contains("雪")) {
            return HomeWeatherInsight(
                primaryTitle = "天气变化提醒",
                primarySubtitle = "$condition，外出建议携带雨具并关注路面湿滑。",
                isPrimaryOpensAlerts = false,
                secondaryTitle = "出行建议",
                secondarySubtitle = data.travelAdvice
            )
        }
        val windScale = WindScaleUtils.parseDisplayScale(data.windScale)
        if (windScale >= 5) {
            return HomeWeatherInsight(
                primaryTitle = "风力提醒",
                primarySubtitle = "${data.windDir}${data.windScale}级，户外注意高空坠物和骑行安全。",
                isPrimaryOpensAlerts = false,
                secondaryTitle = "出行建议",
                secondarySubtitle = data.travelAdvice
            )
        }
        val uvIndex = data.uvIndex.takeIf { value -> value.isNotBlank() }?.toInt()
        if (uvIndex != null && uvIndex >= 8) {
            return HomeWeatherInsight(
                primaryTitle = "紫外线提醒",
                primarySubtitle = "UV $uvIndex 偏强，外出建议防晒并减少暴晒。",
                isPrimaryOpensAlerts = false,
                secondaryTitle = "穿衣建议",
                secondarySubtitle = data.clothingAdvice
            )
        }
        val airQualityIndex = AirQualityUtils.parseUsAqiDisplay(data.airQualityIndex)
        if (airQualityIndex > 150) {
            return HomeWeatherInsight(
                primaryTitle = "空气质量提醒",
                primarySubtitle = "AQI $airQualityIndex ${data.airQualityCategory}，${AirQualityUtils.activityAdviceForUsAqi(airQualityIndex)}",
                isPrimaryOpensAlerts = false,
                secondaryTitle = "敏感人群提示",
                secondarySubtitle = AirQualityUtils.sensitiveGroupAdviceForUsAqi(airQualityIndex)
            )
        }
        return HomeWeatherInsight(
            primaryTitle = "空气质量与出行",
            primarySubtitle = "AQI $airQualityIndex ${data.airQualityCategory}，${data.travelAdvice}",
            isPrimaryOpensAlerts = false,
            secondaryTitle = "穿衣建议",
            secondarySubtitle = data.clothingAdvice
        )
    }
}
