package com.litroenade.yunjiweather.data.model;

import java.util.Map;

public final class CustomThemeResolver {

    private static final int MINUTES_PER_DAY = 24 * 60;

    private CustomThemeResolver() {
    }

    public static CustomThemeAsset resolve(
            CustomThemeProfile profile,
            String weatherKey,
            boolean night,
            int minuteOfDay
    ) {
        if (profile == null || profile.isEmpty()) {
            return CustomThemeAsset.empty();
        }
        Map<String, CustomThemeAsset> assetsById = profile.assetsById();
        String normalizedWeatherKey = CustomThemeWeatherKey.normalize(weatherKey);
        int normalizedMinute = normalizeMinute(minuteOfDay);
        CustomThemeRule bestRule = null;
        CustomThemeRule fallbackRule = null;
        for (CustomThemeRule rule : profile.getRules()) {
            if (!assetsById.containsKey(rule.getAssetId())) {
                continue;
            }
            if (rule.isFallbackRule()) {
                fallbackRule = chooseBetterFallback(fallbackRule, rule);
                continue;
            }
            if (matches(rule, normalizedWeatherKey, night, normalizedMinute)
                    && (bestRule == null || score(rule) > score(bestRule))) {
                bestRule = rule;
            }
        }
        if (bestRule != null) {
            return assetsById.get(bestRule.getAssetId());
        }
        if (fallbackRule != null) {
            return assetsById.get(fallbackRule.getAssetId());
        }
        return profile.getAssets().isEmpty() ? CustomThemeAsset.empty() : profile.getAssets().get(0);
    }

    private static boolean matches(
            CustomThemeRule rule,
            String weatherKey,
            boolean night,
            int minuteOfDay
    ) {
        if (!CustomThemeWeatherKey.FALLBACK.equals(rule.getWeatherKey())
                && !rule.getWeatherKey().equals(weatherKey)) {
            return false;
        }
        if (CustomThemeRule.LIGHT_DAY.equals(rule.getLightMode()) && night) {
            return false;
        }
        if (CustomThemeRule.LIGHT_NIGHT.equals(rule.getLightMode()) && !night) {
            return false;
        }
        return !rule.hasTimeWindow() || isInsideTimeWindow(minuteOfDay, rule.getStartMinute(), rule.getEndMinute());
    }

    private static boolean isInsideTimeWindow(int minuteOfDay, int startMinute, int endMinute) {
        if (startMinute == endMinute) {
            return true;
        }
        if (startMinute < endMinute) {
            return minuteOfDay >= startMinute && minuteOfDay < endMinute;
        }
        return minuteOfDay >= startMinute || minuteOfDay < endMinute;
    }

    private static int score(CustomThemeRule rule) {
        int score = rule.getPriority() * 100;
        if (!CustomThemeWeatherKey.FALLBACK.equals(rule.getWeatherKey())) {
            score += 10;
        }
        if (!CustomThemeRule.LIGHT_ANY.equals(rule.getLightMode())) {
            score += 5;
        }
        if (rule.hasTimeWindow()) {
            score += 2;
        }
        return score;
    }

    private static CustomThemeRule chooseBetterFallback(CustomThemeRule current, CustomThemeRule candidate) {
        if (current == null || candidate.getPriority() > current.getPriority()) {
            return candidate;
        }
        return current;
    }

    private static int normalizeMinute(int minute) {
        int normalized = minute % MINUTES_PER_DAY;
        return normalized < 0 ? normalized + MINUTES_PER_DAY : normalized;
    }
}
