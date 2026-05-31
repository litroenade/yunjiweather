package com.litroenade.yunjiweather.ui.location;

/**
 * Location entry UI state for city and widget screens.
 */
public final class LocationUiState {

    private static final LocationUiState IDLE = new LocationUiState(LocationStatus.IDLE, "");
    private final LocationStatus status;
    private final String message;

    private LocationUiState(LocationStatus status, String message) {
        this.status = status;
        this.message = message == null ? "" : message;
    }

    public static LocationUiState idle() {
        return IDLE;
    }

    public static LocationUiState requestingPermission() {
        return new LocationUiState(LocationStatus.REQUESTING_PERMISSION, "\u9700\u8981\u5b9a\u4f4d\u6743\u9650\u6765\u8bc6\u522b\u5f53\u524d\u57ce\u5e02\u3002");
    }

    public static LocationUiState fetchingLocation() {
        return new LocationUiState(LocationStatus.FETCHING_LOCATION, "\u6b63\u5728\u83b7\u53d6\u7cfb\u7edf\u5b9a\u4f4d\u3002");
    }

    public static LocationUiState success(String message) {
        return new LocationUiState(LocationStatus.SUCCESS, message);
    }

    public static LocationUiState error(String message) {
        return new LocationUiState(LocationStatus.ERROR, message);
    }

    public static LocationUiState denied() {
        return new LocationUiState(LocationStatus.DENIED, "\u672a\u6388\u4e88\u5b9a\u4f4d\u6743\u9650\uff0c\u53ef\u624b\u52a8\u641c\u7d22\u5e76\u6dfb\u52a0\u57ce\u5e02\u3002");
    }

    public LocationStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isBusy() {
        return status == LocationStatus.REQUESTING_PERMISSION || status == LocationStatus.FETCHING_LOCATION;
    }
}
