package com.litroenade.yunjiweather.ui.compose.theme;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class YunJiUiTokensTest {

    @Test
    public void pageTokensKeepSharedScreenAlignmentStable() {
        assertEquals(20, YunJiUiTokens.SCREEN_HORIZONTAL_PADDING_DP);
        assertEquals(24, YunJiUiTokens.PAGE_HEADER_HORIZONTAL_PADDING_DP);
        assertEquals(40, YunJiUiTokens.PAGE_HEADER_ICON_BUTTON_DP);
        assertEquals(22, YunJiUiTokens.PAGE_HEADER_ICON_DP);
        assertEquals(112, YunJiUiTokens.IMMERSIVE_CONTENT_TOP_PADDING_DP);
        assertEquals(28, YunJiUiTokens.BOTTOM_ACTION_HORIZONTAL_PADDING_DP);
        assertEquals(58, YunJiUiTokens.PRIMARY_BUTTON_HEIGHT_DP);
    }

    @Test
    public void textTokensKeepPageHierarchyStable() {
        assertEquals(20, YunJiUiTokens.PAGE_HEADER_TITLE_SP);
        assertEquals(26, YunJiUiTokens.PAGE_HEADER_TITLE_LINE_HEIGHT_SP);
        assertEquals(16, YunJiUiTokens.PAGE_TAB_TEXT_SP);
        assertEquals(16, YunJiUiTokens.PRIMARY_ACTION_TEXT_SP);
        assertEquals(16, YunJiUiTokens.SECTION_TITLE_SP);
        assertTrue(YunJiUiTokens.PAGE_TAB_TEXT_SP < YunJiUiTokens.PAGE_HEADER_TITLE_SP);
        assertTrue(YunJiUiTokens.PRIMARY_ACTION_TEXT_SP < YunJiUiTokens.PAGE_HEADER_TITLE_SP);
        assertTrue(YunJiUiTokens.SECTION_TITLE_SP <= YunJiUiTokens.PRIMARY_ACTION_TEXT_SP);
    }
}
