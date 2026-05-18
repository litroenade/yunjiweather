package com.litroenade.yunjiweather.ui.index;

import com.litroenade.yunjiweather.data.api.model.QWeatherIndicesResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class LifeIndexMapper {

    private LifeIndexMapper() {
    }

    public static List<LifeIndexItem> mapDailyIndices(List<QWeatherIndicesResponse.Daily> dailyList) throws IOException {
        if (dailyList == null || dailyList.isEmpty()) {
            throw new IOException("生活指数接口缺少 daily 数据");
        }
        List<LifeIndexItem> result = new ArrayList<>();
        for (QWeatherIndicesResponse.Daily daily : dailyList) {
            if (daily == null) {
                continue;
            }
            String advice = requireText(daily.text, "indices.daily.text");
            result.add(new LifeIndexItem(
                    requireText(daily.name, "indices.daily.name"),
                    requireText(daily.category, "indices.daily.category"),
                    advice,
                    advice
            ));
        }
        if (result.isEmpty()) {
            throw new IOException("生活指数接口没有可展示数据");
        }
        return result;
    }

    private static String requireText(String value, String fieldName) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            throw new IOException("生活指数接口缺少字段：" + fieldName);
        }
        return value;
    }
}
