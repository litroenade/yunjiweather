package com.litroenade.yunjiweather.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CustomThemeProfile {

    private static final CustomThemeProfile EMPTY = new CustomThemeProfile(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptySet()
    );

    private final List<CustomThemeAsset> assets;
    private final List<CustomThemeRule> rules;
    private final List<String> homeModuleOrder;
    private final Set<String> disabledHomeModules;

    private CustomThemeProfile(
            List<CustomThemeAsset> assets,
            List<CustomThemeRule> rules,
            List<String> homeModuleOrder,
            Set<String> disabledHomeModules
    ) {
        this.assets = Collections.unmodifiableList(assets);
        this.rules = Collections.unmodifiableList(rules);
        this.homeModuleOrder = Collections.unmodifiableList(homeModuleOrder);
        this.disabledHomeModules = Collections.unmodifiableSet(disabledHomeModules);
    }

    public static CustomThemeProfile empty() {
        return EMPTY;
    }

    public static CustomThemeProfile create(
            List<CustomThemeAsset> assets,
            List<CustomThemeRule> rules,
            List<String> homeModuleOrder,
            Set<String> disabledHomeModules
    ) {
        List<CustomThemeAsset> sanitizedAssets = sanitizeAssets(assets);
        List<CustomThemeRule> sanitizedRules = sanitizeRules(rules, sanitizedAssets);
        return new CustomThemeProfile(
                sanitizedAssets,
                sanitizedRules,
                sanitizeStringList(homeModuleOrder),
                sanitizeStringSet(disabledHomeModules)
        );
    }

    public boolean isEmpty() {
        return assets.isEmpty() && rules.isEmpty() && homeModuleOrder.isEmpty() && disabledHomeModules.isEmpty();
    }

    public List<CustomThemeAsset> getAssets() {
        return assets;
    }

    public List<CustomThemeRule> getRules() {
        return rules;
    }

    public List<String> getHomeModuleOrder() {
        return homeModuleOrder;
    }

    public Set<String> getDisabledHomeModules() {
        return disabledHomeModules;
    }

    public Map<String, CustomThemeAsset> assetsById() {
        Map<String, CustomThemeAsset> result = new LinkedHashMap<>();
        for (CustomThemeAsset asset : assets) {
            result.put(asset.getId(), asset);
        }
        return result;
    }

    private static List<CustomThemeAsset> sanitizeAssets(List<CustomThemeAsset> assets) {
        if (assets == null || assets.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, CustomThemeAsset> result = new LinkedHashMap<>();
        for (CustomThemeAsset asset : assets) {
            if (asset != null && !asset.isEmpty()) {
                result.put(asset.getId(), asset);
            }
        }
        return new ArrayList<>(result.values());
    }

    private static List<CustomThemeRule> sanitizeRules(List<CustomThemeRule> rules, List<CustomThemeAsset> assets) {
        if (rules == null || rules.isEmpty() || assets.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> assetIds = new HashSet<>();
        for (CustomThemeAsset asset : assets) {
            assetIds.add(asset.getId());
        }
        List<CustomThemeRule> result = new ArrayList<>();
        for (CustomThemeRule rule : rules) {
            if (rule != null && assetIds.contains(rule.getAssetId())) {
                result.add(rule);
            }
        }
        return result;
    }

    private static List<String> sanitizeStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String value : values) {
            String normalized = value == null ? "" : value.trim();
            if (!normalized.isEmpty() && seen.add(normalized)) {
                result.add(normalized);
            }
        }
        return result;
    }

    private static Set<String> sanitizeStringSet(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> result = new LinkedHashSet<>();
        for (String value : values) {
            String normalized = value == null ? "" : value.trim();
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }
}
