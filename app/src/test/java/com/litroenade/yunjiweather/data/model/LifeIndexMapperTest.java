package com.litroenade.yunjiweather.data.model;

import com.litroenade.yunjiweather.data.api.model.QWeatherIndicesResponse;

import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LifeIndexMapperTest {

    @Test
    public void mapDailyIndices_returnsConcreteLifeIndexItems() throws IOException {
        QWeatherIndicesResponse.Daily clothing = daily("3", "穿衣", "舒适", "建议穿短袖或薄外套。");
        QWeatherIndicesResponse.Daily travel = daily("6", "旅游", "适宜", "适合安排短途出游。");

        List<LifeIndexItem> result = LifeIndexMapper.mapDailyIndices(Arrays.asList(clothing, travel));

        assertEquals(2, result.size());
        assertEquals("穿衣", result.get(0).getName());
        assertEquals("舒适", result.get(0).getLevel());
        assertEquals("建议穿短袖或薄外套。", result.get(0).getAdvice());
        assertEquals("建议穿短袖或薄外套。", result.get(0).getDetail());
        assertEquals("旅游", result.get(1).getName());
        assertEquals("适宜", result.get(1).getLevel());
    }

    @Test(expected = IOException.class)
    public void mapDailyIndices_throwsWhenRequiredTextMissing() throws IOException {
        QWeatherIndicesResponse.Daily item = daily("3", "穿衣", "舒适", "");

        LifeIndexMapper.mapDailyIndices(Arrays.asList(item));
    }

    private static QWeatherIndicesResponse.Daily daily(String type, String name, String category, String text) {
        QWeatherIndicesResponse.Daily daily = new QWeatherIndicesResponse.Daily();
        daily.type = type;
        daily.name = name;
        daily.category = category;
        daily.text = text;
        return daily;
    }
}
