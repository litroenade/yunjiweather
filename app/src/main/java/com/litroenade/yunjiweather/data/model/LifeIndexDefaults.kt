package com.litroenade.yunjiweather.data.model

import com.litroenade.yunjiweather.utils.WeatherAdviceUtils

object LifeIndexDefaults {

    @JvmStatic
    fun createFallbackItems(): List<LifeIndexItem> {
        return createDefaultMap().values.toList()
    }

    @JvmStatic
    fun completeWithFallbacks(remoteItems: List<LifeIndexItem>): List<LifeIndexItem> {
        val result = createDefaultMap()
        remoteItems.filterNotNull().forEach { item ->
            val normalizedName = normalizeName(item.name)
            if (result.containsKey(normalizedName)) {
                result[normalizedName] = item
            }
        }
        return result.values.toList()
    }

    private fun createDefaultMap(): LinkedHashMap<String, LifeIndexItem> {
        val result = linkedMapOf<String, LifeIndexItem>()
        put(result, LifeIndexItem("穿衣", "舒适", WeatherAdviceUtils.generateClothingAdvice(24, "多云", 2, 70), "结合温度、天气、风力和空气质量生成穿衣建议。"))
        put(result, LifeIndexItem("出行", "适宜", WeatherAdviceUtils.generateTravelAdvice(24, "多云", 2, 70, 3, false), "弱风和良好空气质量下适合日常通勤出行。"))
        put(result, LifeIndexItem("运动", "适宜", "适合户外慢跑和轻量运动。", "高温、强风、降雨和空气污染都会降低运动建议等级。"))
        put(result, LifeIndexItem("洗车", "较适宜", "短时间无明显降雨，可安排洗车。", "如果未来有雨雪天气，建议推迟洗车。"))
        put(result, LifeIndexItem("紫外线", "中等", "午后外出建议涂抹防晒。", "紫外线指数偏高时需要减少长时间暴晒。"))
        put(result, LifeIndexItem("感冒", "较低", "昼夜温差变化时注意加衣。", "低温、强风和降雨会提高感冒风险。"))
        put(result, LifeIndexItem("空气", "良", "空气质量可以接受，敏感人群适当防护。", "空气质量指数升高时建议减少户外停留。"))
        put(result, LifeIndexItem("舒适度", "舒适", "体感较舒适，适合外出活动。", "舒适度结合温度、湿度、风力和天气状况评估。"))
        put(result, LifeIndexItem("晾晒", "较适宜", "衣物可正常晾晒，注意关注短时天气变化。", "降雨、湿度偏高或空气污染时建议减少户外晾晒。"))
        put(result, LifeIndexItem("旅游", "适宜", "适合安排短途游览，出门前留意实时天气。", "旅游建议结合降雨、风力、气温和预警信息综合判断。"))
        return LinkedHashMap(result)
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
