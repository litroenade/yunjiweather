package com.litroenade.yunjiweather.ui.alert;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WarningStyleUtilsTest {

    @Test
    public void resolveColor_supportsChineseWarningLevels() {
        assertEquals("#155EEF", WarningStyleUtils.resolveColorHex("蓝色预警"));
        assertEquals("#B54708", WarningStyleUtils.resolveColorHex("黄色预警"));
        assertEquals("#C4320A", WarningStyleUtils.resolveColorHex("橙色预警"));
        assertEquals("#B42318", WarningStyleUtils.resolveColorHex("红色预警"));
    }

    @Test
    public void resolveColor_supportsEnglishWarningLevels() {
        assertEquals("#155EEF", WarningStyleUtils.resolveColorHex("blue"));
        assertEquals("#B54708", WarningStyleUtils.resolveColorHex("yellow"));
        assertEquals("#C4320A", WarningStyleUtils.resolveColorHex("orange"));
        assertEquals("#B42318", WarningStyleUtils.resolveColorHex("red"));
    }

    @Test
    public void resolveColor_unknownLevelUsesNeutralColor() {
        assertEquals("#667085", WarningStyleUtils.resolveColorHex("unknown"));
    }
}
