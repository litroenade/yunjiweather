package com.litroenade.yunjiweather.ui.location;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LocationUiStateTest {

    @Test
    public void factoryMessagesAreReadableChinese() {
        assertEquals("\u9700\u8981\u5b9a\u4f4d\u6743\u9650\u6765\u8bc6\u522b\u5f53\u524d\u57ce\u5e02\u3002", LocationUiState.requestingPermission().getMessage());
        assertEquals("\u6b63\u5728\u83b7\u53d6\u7cfb\u7edf\u5b9a\u4f4d\u3002", LocationUiState.fetchingLocation().getMessage());
        assertEquals("\u672a\u6388\u4e88\u5b9a\u4f4d\u6743\u9650\uff0c\u53ef\u624b\u52a8\u641c\u7d22\u5e76\u6dfb\u52a0\u57ce\u5e02\u3002", LocationUiState.denied().getMessage());
    }
}
