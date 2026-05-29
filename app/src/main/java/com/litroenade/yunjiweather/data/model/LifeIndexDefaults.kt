package com.litroenade.yunjiweather.data.model

import com.litroenade.yunjiweather.utils.WeatherAdviceUtils
import kotlin.math.max

/**
 * 本地生活建议规则集合。
 * 规则只依赖首页天气缓存中的基础字段，避免因外部指数接口缺失导致页面不可用。
 */
object LifeIndexDefaults {

    private const val DEFAULT_TEMPERATURE = 24
    private const val DEFAULT_CONDITION = "多云"
    private const val DEFAULT_WIND_SCALE = 2
    private const val DEFAULT_AQI = 70
    private const val DEFAULT_UV = 3

    @JvmStatic
    fun createFallbackItems(): List<LifeIndexItem> {
        return createItems(
            temperature = DEFAULT_TEMPERATURE,
            condition = DEFAULT_CONDITION,
            windScale = DEFAULT_WIND_SCALE,
            airQualityIndex = DEFAULT_AQI,
            uvIndex = DEFAULT_UV,
            hasWarning = false
        )
    }

    @JvmStatic
    fun createWeatherDrivenItems(weather: HomeWeatherData): List<LifeIndexItem> {
        return createItems(
            temperature = parseIntOrDefault(weather.temperature, DEFAULT_TEMPERATURE),
            condition = weather.condition.ifBlank { DEFAULT_CONDITION },
            windScale = parseIntOrDefault(weather.windScale, DEFAULT_WIND_SCALE),
            airQualityIndex = parseIntOrDefault(weather.airQualityIndex, DEFAULT_AQI),
            uvIndex = parseIntOrDefault(weather.uvIndex, DEFAULT_UV),
            hasWarning = false
        )
    }

    @JvmStatic
    fun completeWithFallbacks(remoteItems: List<LifeIndexItem>): List<LifeIndexItem> {
        val result = createDefaultMap()
        remoteItems.forEach { item ->
            val normalizedName = normalizeName(item.name)
            if (result.containsKey(normalizedName)) {
                result[normalizedName] = item
            }
        }
        return result.values.toList()
    }

    private fun createDefaultMap(): LinkedHashMap<String, LifeIndexItem> {
        val result = linkedMapOf<String, LifeIndexItem>()
        createFallbackItems().forEach { put(result, it) }
        return LinkedHashMap(result)
    }

    private fun createItems(
        temperature: Int,
        condition: String,
        windScale: Int,
        airQualityIndex: Int,
        uvIndex: Int,
        hasWarning: Boolean
    ): List<LifeIndexItem> {
        val rainOrSnow = hasPrecipitation(condition)
        val highWind = windScale >= 5
        val poorAir = airQualityIndex >= 150
        return listOf(
            LifeIndexItem(
                "穿衣",
                clothingLevel(temperature, windScale, rainOrSnow),
                WeatherAdviceUtils.generateClothingAdvice(temperature, condition, windScale, airQualityIndex),
                "结合当前温度、天气、风力和空气质量生成穿衣建议。"
            ),
            LifeIndexItem(
                "出行",
                travelLevel(rainOrSnow, highWind, poorAir, hasWarning),
                WeatherAdviceUtils.generateTravelAdvice(
                    temperature,
                    condition,
                    windScale,
                    airQualityIndex,
                    uvIndex,
                    hasWarning
                ),
                "雨雪、强风、污染和预警会降低日常通勤建议等级。"
            ),
            LifeIndexItem(
                "运动",
                sportLevel(temperature, rainOrSnow, highWind, poorAir),
                sportAdvice(temperature, rainOrSnow, highWind, poorAir),
                "根据温度、降水、风力和空气质量评估户外运动强度。"
            ),
            LifeIndexItem(
                "洗车",
                carWashLevel(rainOrSnow, highWind),
                carWashAdvice(rainOrSnow, highWind),
                "有雨雪或大风时不建议洗车，避免短时间内再次弄脏。"
            ),
            LifeIndexItem(
                "紫外线",
                uvLevel(uvIndex),
                uvAdvice(uvIndex),
                "紫外线越高，越需要减少暴晒并补充遮阳防护。"
            ),
            LifeIndexItem(
                "感冒",
                coldRiskLevel(temperature, windScale, rainOrSnow),
                coldRiskAdvice(temperature, windScale, rainOrSnow),
                "低温、强风和雨雪会提高着凉风险。"
            ),
            LifeIndexItem(
                "空气",
                airLevel(airQualityIndex),
                airAdvice(airQualityIndex),
                "空气质量指数升高时，敏感人群应减少户外停留。"
            ),
            LifeIndexItem(
                "舒适度",
                comfortLevel(temperature, rainOrSnow, highWind, poorAir),
                comfortAdvice(temperature, rainOrSnow, highWind, poorAir),
                "体感舒适度综合温度、降水、风力和空气质量。"
            ),
            LifeIndexItem(
                "晾晒",
                dryingLevel(rainOrSnow, windScale, airQualityIndex),
                dryingAdvice(rainOrSnow, windScale, airQualityIndex),
                "降水、空气湿度和污染会影响衣物晾晒效果。"
            ),
            LifeIndexItem(
                "旅游",
                tourismLevel(temperature, rainOrSnow, highWind, poorAir, hasWarning),
                tourismAdvice(temperature, rainOrSnow, highWind, poorAir, hasWarning),
                "旅游建议结合天气风险和户外停留舒适度生成。"
            )
        )
    }

    private fun clothingLevel(temperature: Int, windScale: Int, rainOrSnow: Boolean): String {
        return when {
            temperature <= 5 || (temperature <= 10 && windScale >= 4) -> "寒冷"
            temperature <= 15 || rainOrSnow -> "偏凉"
            temperature >= 33 -> "炎热"
            temperature >= 28 -> "偏热"
            else -> "舒适"
        }
    }

    private fun travelLevel(rainOrSnow: Boolean, highWind: Boolean, poorAir: Boolean, hasWarning: Boolean): String {
        return when {
            hasWarning || highWind -> "谨慎"
            rainOrSnow || poorAir -> "一般"
            else -> "适宜"
        }
    }

    private fun sportLevel(temperature: Int, rainOrSnow: Boolean, highWind: Boolean, poorAir: Boolean): String {
        return when {
            rainOrSnow || highWind || poorAir -> "不宜"
            temperature <= 0 || temperature >= 34 -> "谨慎"
            temperature in 12..28 -> "适宜"
            else -> "较适宜"
        }
    }

    private fun sportAdvice(temperature: Int, rainOrSnow: Boolean, highWind: Boolean, poorAir: Boolean): String {
        return when {
            rainOrSnow -> "有明显降水，建议改为室内拉伸或力量训练。"
            highWind -> "风力偏大，户外运动注意避开高空坠物和空旷地带。"
            poorAir -> "空气质量偏差，建议降低户外运动时长。"
            temperature >= 34 -> "高温天气运动需避开午后并及时补水。"
            temperature <= 0 -> "低温天气运动前充分热身，注意防滑保暖。"
            else -> "适合安排慢跑、骑行或轻量户外活动。"
        }
    }

    private fun carWashLevel(rainOrSnow: Boolean, highWind: Boolean): String {
        return when {
            rainOrSnow -> "不宜"
            highWind -> "较不宜"
            else -> "较适宜"
        }
    }

    private fun carWashAdvice(rainOrSnow: Boolean, highWind: Boolean): String {
        return when {
            rainOrSnow -> "当前有降水迹象，建议推迟洗车。"
            highWind -> "风力偏大，洗车后容易附着浮尘。"
            else -> "短时间无明显雨雪，可安排洗车。"
        }
    }

    private fun uvLevel(uvIndex: Int): String {
        return when {
            uvIndex >= 8 -> "很强"
            uvIndex >= 6 -> "强"
            uvIndex >= 3 -> "中等"
            else -> "较弱"
        }
    }

    private fun uvAdvice(uvIndex: Int): String {
        return when {
            uvIndex >= 8 -> "紫外线很强，外出建议遮阳、涂防晒并减少暴晒。"
            uvIndex >= 6 -> "紫外线偏强，午后外出建议做好防晒。"
            uvIndex >= 3 -> "紫外线中等，长时间户外活动建议基础防护。"
            else -> "紫外线较弱，日常通勤保持基础防护即可。"
        }
    }

    private fun coldRiskLevel(temperature: Int, windScale: Int, rainOrSnow: Boolean): String {
        val risk = max(0, 12 - temperature) + if (windScale >= 4) 4 else 0 + if (rainOrSnow) 3 else 0
        return when {
            risk >= 14 -> "较高"
            risk >= 8 -> "中等"
            else -> "较低"
        }
    }

    private fun coldRiskAdvice(temperature: Int, windScale: Int, rainOrSnow: Boolean): String {
        return when {
            temperature <= 8 && windScale >= 4 -> "低温叠加强风，建议增加防风保暖衣物。"
            temperature <= 12 || rainOrSnow -> "早晚或雨雪时体感偏凉，注意及时加衣。"
            else -> "感冒风险较低，保持正常作息和补水。"
        }
    }

    private fun airLevel(airQualityIndex: Int): String {
        return when {
            airQualityIndex <= 50 -> "优"
            airQualityIndex <= 100 -> "良"
            airQualityIndex <= 150 -> "轻度"
            airQualityIndex <= 200 -> "中度"
            else -> "较差"
        }
    }

    private fun airAdvice(airQualityIndex: Int): String {
        return when {
            airQualityIndex <= 50 -> "空气质量优秀，适合开窗通风和户外活动。"
            airQualityIndex <= 100 -> "空气质量良好，敏感人群按需防护。"
            airQualityIndex <= 150 -> "轻度污染，敏感人群建议减少长时间户外停留。"
            airQualityIndex <= 200 -> "污染较明显，户外活动建议佩戴口罩。"
            else -> "空气质量较差，建议减少户外活动并关闭门窗。"
        }
    }

    private fun comfortLevel(temperature: Int, rainOrSnow: Boolean, highWind: Boolean, poorAir: Boolean): String {
        return when {
            rainOrSnow || highWind || poorAir -> "一般"
            temperature in 18..27 -> "舒适"
            temperature in 12..32 -> "较舒适"
            else -> "不舒适"
        }
    }

    private fun comfortAdvice(temperature: Int, rainOrSnow: Boolean, highWind: Boolean, poorAir: Boolean): String {
        return when {
            rainOrSnow -> "降水会降低户外舒适度，建议携带雨具。"
            highWind -> "风力偏大，户外体感会更冷或更干。"
            poorAir -> "空气质量偏差，长时间外出舒适度下降。"
            temperature in 18..27 -> "体感舒适，适合通勤、散步和短途外出。"
            temperature > 32 -> "体感偏热，外出注意补水降温。"
            else -> "体感偏凉，建议根据早晚温差增减衣物。"
        }
    }

    private fun dryingLevel(rainOrSnow: Boolean, windScale: Int, airQualityIndex: Int): String {
        return when {
            rainOrSnow -> "不宜"
            airQualityIndex >= 150 -> "一般"
            windScale in 2..4 -> "适宜"
            else -> "较适宜"
        }
    }

    private fun dryingAdvice(rainOrSnow: Boolean, windScale: Int, airQualityIndex: Int): String {
        return when {
            rainOrSnow -> "有降水，衣物建议室内晾晒或烘干。"
            airQualityIndex >= 150 -> "空气质量偏差，贴身衣物建议减少户外晾晒。"
            windScale in 2..4 -> "风力适中，适合户外晾晒。"
            else -> "可正常晾晒，注意关注短时天气变化。"
        }
    }

    private fun tourismLevel(
        temperature: Int,
        rainOrSnow: Boolean,
        highWind: Boolean,
        poorAir: Boolean,
        hasWarning: Boolean
    ): String {
        return when {
            hasWarning || highWind -> "谨慎"
            rainOrSnow || poorAir || temperature !in 5..34 -> "一般"
            else -> "适宜"
        }
    }

    private fun tourismAdvice(
        temperature: Int,
        rainOrSnow: Boolean,
        highWind: Boolean,
        poorAir: Boolean,
        hasWarning: Boolean
    ): String {
        return when {
            hasWarning -> "存在天气风险提示，出游前建议重新确认路线和预警。"
            highWind -> "风力偏大，山地、滨水和高空项目需谨慎。"
            rainOrSnow -> "雨雪会影响游览体验，建议准备雨具并缩短户外停留。"
            poorAir -> "空气质量偏差，建议选择室内场馆或短途路线。"
            temperature >= 34 -> "高温天气出游需避开午后并做好防暑。"
            temperature <= 5 -> "低温天气出游注意保暖和路面结冰。"
            else -> "适合安排短途游览，出门前留意实时天气。"
        }
    }

    private fun hasPrecipitation(condition: String): Boolean {
        val text = condition.lowercase()
        return listOf("雨", "雪", "雷", "rain", "snow", "sleet", "shower", "thunder").any { it in text }
    }

    private fun parseIntOrDefault(value: String, defaultValue: Int): Int {
        val match = Regex("-?\\d+").find(value)
        return match?.value?.toIntOrNull() ?: defaultValue
    }

    private fun put(target: MutableMap<String, LifeIndexItem>, item: LifeIndexItem) {
        target[normalizeName(item.name)] = item
    }

    private fun normalizeName(name: String): String {
        val text = name.trim()
        return when (text) {
            "穿衣指数" -> "穿衣"
            "出行指数" -> "出行"
            "运动指数" -> "运动"
            "洗车指数" -> "洗车"
            "旅游", "旅行", "旅游指数" -> "旅游"
            "紫外线", "防晒", "紫外线指数" -> "紫外线"
            "感冒指数" -> "感冒"
            "空气质量", "空气污染扩散条件" -> "空气"
            "舒适", "舒适度", "舒适度指数" -> "舒适度"
            "晾晒指数" -> "晾晒"
            else -> text
        }
    }
}
