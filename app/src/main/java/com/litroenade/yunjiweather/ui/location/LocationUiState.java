package com.litroenade.yunjiweather.ui.location;

/**
 * 定位入口的界面状态机。
 * 城市页面根据状态展示授权、定位中、失败和手动搜索降级提示。
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
        return new LocationUiState(LocationStatus.REQUESTING_PERMISSION, "需要定位权限来识别当前城市。");
    }

    public static LocationUiState fetchingLocation() {
        return new LocationUiState(LocationStatus.FETCHING_LOCATION, "正在获取系统定位。");
    }

    public static LocationUiState success(String message) {
        return new LocationUiState(LocationStatus.SUCCESS, message);
    }

    public static LocationUiState error(String message) {
        return new LocationUiState(LocationStatus.ERROR, message);
    }

    public static LocationUiState denied() {
        return new LocationUiState(LocationStatus.DENIED, "未授予定位权限，可手动搜索并添加城市。");
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
