package com.litroenade.yunjiweather.data.model;

import com.litroenade.yunjiweather.utils.WeatherAdviceUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class LifeIndexDefaults {

    private LifeIndexDefaults() {
    }

    public static List<LifeIndexItem> createFallbackItems() {
        return new ArrayList<>(createDefaultMap().values());
    }

    public static List<LifeIndexItem> completeWithFallbacks(List<LifeIndexItem> remoteItems) {
        Objects.requireNonNull(remoteItems, "remoteItems");
        LinkedHashMap<String, LifeIndexItem> result = createDefaultMap();
        for (LifeIndexItem item : remoteItems) {
            if (item == null) {
                continue;
            }
            String normalizedName = normalizeName(item.getName());
            if (result.containsKey(normalizedName)) {
                result.put(normalizedName, item);
            }
        }
        return new ArrayList<>(result.values());
    }

    private static LinkedHashMap<String, LifeIndexItem> createDefaultMap() {
        LinkedHashMap<String, LifeIndexItem> result = new LinkedHashMap<>();
        put(result, new LifeIndexItem("穿衣", "舒适", WeatherAdviceUtils.generateClothingAdvice(24, "多云", 2, 70), "结合温度、天气、风力和空气质量生成穿衣建议。"));
        put(result, new LifeIndexItem("出行", "适宜", WeatherAdviceUtils.generateTravelAdvice(24, "多云", 2, 70, 3, false), "弱风和良好空气质量下适合日常通勤出行。"));
        put(result, new LifeIndexItem("运动", "适宜", "适合户外慢跑和轻量运动。", "高温、强风、降雨和空气污染都会降低运动建议等级。"));
        put(result, new LifeIndexItem("洗车", "较适宜", "短时间无明显降雨，可安排洗车。", "如果未来有雨雪天气，建议推迟洗车。"));
        put(result, new LifeIndexItem("紫外线", "中等", "午后外出建议涂抹防晒。", "紫外线指数偏高时需要减少长时间暴晒。"));
        put(result, new LifeIndexItem("感冒", "较低", "昼夜温差变化时注意加衣。", "低温、强风和降雨会提高感冒风险。"));
        put(result, new LifeIndexItem("空气", "良", "空气质量可以接受，敏感人群适当防护。", "空气质量指数升高时建议减少户外停留。"));
        put(result, new LifeIndexItem("舒适度", "舒适", "体感较舒适，适合外出活动。", "舒适度结合温度、湿度、风力和天气状况评估。"));
        put(result, new LifeIndexItem("晾晒", "较适宜", "衣物可正常晾晒，注意关注短时天气变化。", "降雨、湿度偏高或空气污染时建议减少户外晾晒。"));
        put(result, new LifeIndexItem("旅游", "适宜", "适合安排短途游览，出门前留意实时天气。", "旅游建议结合降雨、风力、气温和预警信息综合判断。"));
        return result;
    }

    private static void put(Map<String, LifeIndexItem> target, LifeIndexItem item) {
        target.put(normalizeName(item.getName()), item);
    }

    private static String normalizeName(String name) {
        String text = Objects.requireNonNull(name, "name").trim();
        if ("穿衣指数".equals(text)) {
            return "穿衣";
        }
        if ("出行指数".equals(text)) {
            return "出行";
        }
        if ("运动指数".equals(text)) {
            return "运动";
        }
        if ("洗车指数".equals(text)) {
            return "洗车";
        }
        if ("旅游".equals(text) || "旅行".equals(text) || "旅游指数".equals(text)) {
            return "旅游";
        }
        if ("紫外线".equals(text) || "防晒".equals(text) || "紫外线指数".equals(text)) {
            return "紫外线";
        }
        if ("感冒指数".equals(text)) {
            return "感冒";
        }
        if ("空气质量".equals(text) || "空气污染扩散条件".equals(text)) {
            return "空气";
        }
        if ("舒适".equals(text) || "舒适度".equals(text) || "舒适度指数".equals(text)) {
            return "舒适度";
        }
        if ("晾晒指数".equals(text)) {
            return "晾晒";
        }
        return text;
    }
}
