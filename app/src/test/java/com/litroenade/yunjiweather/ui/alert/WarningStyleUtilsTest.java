package com.litroenade.yunjiweather.ui.alert;

import com.litroenade.yunjiweather.R;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WarningStyleUtilsTest {

    @Test
    public void resolveColor_supportsChineseWarningLevels() {
        assertEquals("#2F6DAE", WarningStyleUtils.resolveColorHex("蓝色预警"));
        assertEquals("#9A6A00", WarningStyleUtils.resolveColorHex("黄色预警"));
        assertEquals("#B85C1E", WarningStyleUtils.resolveColorHex("橙色预警"));
        assertEquals("#B42318", WarningStyleUtils.resolveColorHex("红色预警"));
    }

    @Test
    public void resolveColor_supportsEnglishWarningLevels() {
        assertEquals("#2F6DAE", WarningStyleUtils.resolveColorHex("blue"));
        assertEquals("#9A6A00", WarningStyleUtils.resolveColorHex("yellow"));
        assertEquals("#B85C1E", WarningStyleUtils.resolveColorHex("orange"));
        assertEquals("#B42318", WarningStyleUtils.resolveColorHex("red"));
    }

    @Test
    public void resolveColor_unknownLevelUsesNeutralColor() {
        assertEquals("#6B7280", WarningStyleUtils.resolveColorHex("unknown"));
    }

    @Test
    public void resolveColorRes_usesWarningColorResources() {
        assertEquals(R.color.warning_blue, WarningStyleUtils.resolveColorRes("蓝色预警"));
        assertEquals(R.color.warning_yellow, WarningStyleUtils.resolveColorRes("yellow"));
        assertEquals(R.color.warning_orange, WarningStyleUtils.resolveColorRes("orange"));
        assertEquals(R.color.warning_red, WarningStyleUtils.resolveColorRes("red"));
        assertEquals(R.color.warning_neutral, WarningStyleUtils.resolveColorRes("unknown"));
    }
}
