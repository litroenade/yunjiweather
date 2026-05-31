package com.litroenade.yunjiweather.data.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CustomThemeProfileCodec {

    private static final String VERSION = "v1";
    private static final String SECTION_SEPARATOR = "\n";
    private static final String FIELD_SEPARATOR = "\\|";
    private static final String FIELD_JOINER = "|";
    private static final String LIST_SEPARATOR = ",";

    private CustomThemeProfileCodec() {
    }

    public static String encode(CustomThemeProfile profile) {
        if (profile == null || profile.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder(VERSION);
        builder.append(SECTION_SEPARATOR).append("assets");
        for (CustomThemeAsset asset : profile.getAssets()) {
            builder.append(SECTION_SEPARATOR)
                    .append("a").append(FIELD_JOINER)
                    .append(encodeValue(asset.getId())).append(FIELD_JOINER)
                    .append(encodeValue(asset.getUri())).append(FIELD_JOINER)
                    .append(encodeValue(asset.getMediaType())).append(FIELD_JOINER)
                    .append(encodeValue(asset.getCropAnchor())).append(FIELD_JOINER)
                    .append(encodeValue(asset.getLabel()));
        }
        builder.append(SECTION_SEPARATOR).append("rules");
        for (CustomThemeRule rule : profile.getRules()) {
            builder.append(SECTION_SEPARATOR)
                    .append("r").append(FIELD_JOINER)
                    .append(encodeValue(rule.getAssetId())).append(FIELD_JOINER)
                    .append(encodeValue(rule.getWeatherKey())).append(FIELD_JOINER)
                    .append(encodeValue(rule.getLightMode())).append(FIELD_JOINER)
                    .append(rule.getStartMinute()).append(FIELD_JOINER)
                    .append(rule.getEndMinute()).append(FIELD_JOINER)
                    .append(rule.getPriority());
        }
        builder.append(SECTION_SEPARATOR)
                .append("layout").append(FIELD_JOINER)
                .append(encodeValue(String.join(LIST_SEPARATOR, profile.getHomeModuleOrder()))).append(FIELD_JOINER)
                .append(encodeValue(String.join(LIST_SEPARATOR, profile.getDisabledHomeModules())));
        return builder.toString();
    }

    public static CustomThemeProfile decode(String encodedProfile) {
        if (encodedProfile == null || encodedProfile.trim().isEmpty()) {
            return CustomThemeProfile.empty();
        }
        try {
            String[] lines = encodedProfile.split(SECTION_SEPARATOR);
            if (lines.length == 0 || !VERSION.equals(lines[0])) {
                return CustomThemeProfile.empty();
            }
            List<CustomThemeAsset> assets = new ArrayList<>();
            List<CustomThemeRule> rules = new ArrayList<>();
            List<String> moduleOrder = new ArrayList<>();
            Set<String> disabledModules = new LinkedHashSet<>();
            for (int index = 1; index < lines.length; index++) {
                String line = lines[index];
                if (line.isEmpty() || "assets".equals(line) || "rules".equals(line)) {
                    continue;
                }
                String[] fields = line.split(FIELD_SEPARATOR, -1);
                if ("a".equals(fields[0]) && fields.length == 6) {
                    assets.add(new CustomThemeAsset(
                            decodeValue(fields[1]),
                            decodeValue(fields[2]),
                            decodeValue(fields[3]),
                            decodeValue(fields[4]),
                            decodeValue(fields[5])
                    ));
                } else if ("r".equals(fields[0]) && fields.length == 7) {
                    rules.add(new CustomThemeRule(
                            decodeValue(fields[1]),
                            decodeValue(fields[2]),
                            decodeValue(fields[3]),
                            parseInt(fields[4], CustomThemeRule.NO_TIME),
                            parseInt(fields[5], CustomThemeRule.NO_TIME),
                            parseInt(fields[6], 0)
                    ));
                } else if ("layout".equals(fields[0]) && fields.length == 3) {
                    moduleOrder = decodeStringList(decodeValue(fields[1]));
                    disabledModules = new LinkedHashSet<>(decodeStringList(decodeValue(fields[2])));
                }
            }
            return CustomThemeProfile.create(assets, rules, moduleOrder, disabledModules);
        } catch (IllegalArgumentException exception) {
            return CustomThemeProfile.empty();
        }
    }

    private static String encodeValue(String value) {
        String normalized = value == null ? "" : value;
        return normalized
                .replace("%", "%25")
                .replace("|", "%7C")
                .replace("\n", "%0A")
                .replace(",", "%2C");
    }

    private static String decodeValue(String encodedValue) {
        if (encodedValue == null || encodedValue.isEmpty()) {
            return "";
        }
        return encodedValue
                .replace("%2C", ",")
                .replace("%0A", "\n")
                .replace("%7C", "|")
                .replace("%25", "%");
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    private static List<String> decodeStringList(String encodedList) {
        List<String> result = new ArrayList<>();
        if (encodedList == null || encodedList.trim().isEmpty()) {
            return result;
        }
        String[] values = encodedList.split(LIST_SEPARATOR);
        for (String value : values) {
            String normalized = value.trim();
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }
}
