package com.litroenade.yunjiweather.utils;

public final class WeatherAdviceUtils {

    private WeatherAdviceUtils() {
    }

    public static String generateClothingAdvice(int temperature, String condition, int windScale, int airQualityIndex) {
        if (temperature >= 30) {
            return "建议穿短袖、短裤，并注意防晒和补水。";
        }
        if (temperature >= 24) {
            return "建议穿短袖或薄外套，早晚温差明显时注意加衣。";
        }
        if (temperature >= 18) {
            return "建议穿长袖或薄外套，体感偏凉时及时加衣。";
        }
        if (temperature >= 10) {
            return "建议穿卫衣或夹克，早晚外出注意保暖。";
        }
        if (temperature >= 0) {
            return "建议穿棉衣或厚外套，注意头部和手部保暖。";
        }
        return "建议穿羽绒服，搭配围巾和手套，减少长时间户外停留。";
    }

    public static String generateTravelAdvice(
            int temperature,
            String condition,
            int windScale,
            int airQualityIndex,
            int uvIndex,
            boolean hasWarning
    ) {
        StringBuilder builder = new StringBuilder();
        String weatherText = condition == null ? "" : condition;
        if (weatherText.contains("雨")) {
            builder.append("出行请携带雨伞，路面湿滑，注意慢行。");
        }
        if (weatherText.contains("雪")) {
            appendSentence(builder, "雪天注意防寒保暖，步行和驾车都要放慢速度。");
        }
        if (airQualityIndex >= 150) {
            appendSentence(builder, "空气质量较差，建议减少户外活动。");
        }
        if (uvIndex >= 6) {
            appendSentence(builder, "紫外线较强，外出请做好防晒。");
        }
        if (windScale >= 5) {
            appendSentence(builder, "风力较大，建议减少骑行并远离临时搭建物。");
        }
        if (temperature >= 30) {
            appendSentence(builder, "高温天气请及时补水，避免长时间暴晒。");
        }
        if (hasWarning) {
            appendSentence(builder, "请关注天气预警变化，必要时调整出行计划。");
        }
        if (builder.length() == 0) {
            return "天气适合出行，建议关注实时天气变化。";
        }
        return builder.toString();
    }

    private static void appendSentence(StringBuilder builder, String sentence) {
        if (builder.length() > 0) {
            builder.append(" ");
        }
        builder.append(sentence);
    }
}
