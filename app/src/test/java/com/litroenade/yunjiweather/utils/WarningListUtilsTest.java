package com.litroenade.yunjiweather.utils;

import com.litroenade.yunjiweather.data.entity.WarningEntity;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WarningListUtilsTest {

    @Test
    public void markRead_marksOnlyTargetWarning() {
        WarningEntity first = warning("w-1", false);
        WarningEntity second = warning("w-2", false);

        List<WarningEntity> result = WarningListUtils.markRead(Arrays.asList(first, second), "w-2");

        assertFalse(result.get(0).isRead);
        assertTrue(result.get(1).isRead);
        assertFalse(first.isRead);
        assertFalse(second.isRead);
    }

    @Test
    public void markRead_keepsAlreadyReadWarningRead() {
        WarningEntity first = warning("w-1", true);
        WarningEntity second = warning("w-2", false);

        List<WarningEntity> result = WarningListUtils.markRead(Arrays.asList(first, second), "w-2");

        assertTrue(result.get(0).isRead);
        assertTrue(result.get(1).isRead);
    }

    @Test
    public void createNoQWeatherText_reportsNoLocalCache() {
        String text = WarningListUtils.createNoQWeatherText("北京", Collections.emptyList());

        assertEquals("未配置 QWeather API，北京暂无本地预警缓存。", text);
    }

    @Test
    public void createNoQWeatherText_reportsCachedWarnings() {
        String text = WarningListUtils.createNoQWeatherText("深圳", Arrays.asList(
                warning("w-1", false),
                warning("w-2", true)
        ));

        assertEquals("未配置 QWeather API，已显示深圳 2 条本地预警缓存。", text);
    }

    private static WarningEntity warning(String warningId, boolean isRead) {
        WarningEntity warning = new WarningEntity(
                warningId,
                "101010100",
                "暴雨蓝色预警",
                "暴雨",
                "蓝色",
                "未来两小时可能出现短时强降雨。",
                1_700_000_000_000L,
                isRead,
                false
        );
        warning.id = warningId.hashCode();
        return warning;
    }
}
