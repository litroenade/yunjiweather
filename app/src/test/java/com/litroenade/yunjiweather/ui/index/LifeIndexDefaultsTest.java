package com.litroenade.yunjiweather.ui.index;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class LifeIndexDefaultsTest {

    @Test
    public void createFallbackItems_returnsTenRequiredIndexes() {
        List<LifeIndexItem> items = LifeIndexDefaults.createFallbackItems();

        assertEquals(10, items.size());
        assertEquals("穿衣", items.get(0).getName());
        assertEquals("出行", items.get(1).getName());
        assertEquals("运动", items.get(2).getName());
        assertEquals("洗车", items.get(3).getName());
        assertEquals("紫外线", items.get(4).getName());
        assertEquals("感冒", items.get(5).getName());
        assertEquals("空气", items.get(6).getName());
        assertEquals("舒适度", items.get(7).getName());
        assertEquals("晾晒", items.get(8).getName());
        assertEquals("旅游", items.get(9).getName());
    }

    @Test
    public void completeWithFallbacks_preservesRemoteItemAndAddsMissingIndexes() {
        LifeIndexItem remoteClothing = new LifeIndexItem("穿衣", "炎热", "建议穿短袖并及时补水。", "高温天气需要注意防晒。");

        List<LifeIndexItem> items = LifeIndexDefaults.completeWithFallbacks(Collections.singletonList(remoteClothing));

        assertEquals(10, items.size());
        assertEquals("炎热", items.get(0).getLevel());
        assertEquals("建议穿短袖并及时补水。", items.get(0).getAdvice());
        assertEquals("出行", items.get(1).getName());
        assertEquals("旅游", items.get(9).getName());
    }
}
