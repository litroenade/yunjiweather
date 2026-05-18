package com.litroenade.yunjiweather.common;

public final class UiState<T> {

    public enum Status {
        LOADING,
        SUCCESS,
        ERROR,
        EMPTY,
        CACHE
    }

    private final Status status;
    private final T data;
    private final String message;
    private final long updateTime;

    private UiState(Status status, T data, String message, long updateTime) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.updateTime = updateTime;
    }

    public static <T> UiState<T> loading() {
        return new UiState<>(Status.LOADING, null, "正在加载天气数据", 0L);
    }

    public static <T> UiState<T> success(T data) {
        return new UiState<>(Status.SUCCESS, data, null, 0L);
    }

    public static <T> UiState<T> cache(T data, String message, long updateTime) {
        return new UiState<>(Status.CACHE, data, message, updateTime);
    }

    public static <T> UiState<T> error(String message) {
        return new UiState<>(Status.ERROR, null, message, 0L);
    }

    public static <T> UiState<T> empty(String message) {
        return new UiState<>(Status.EMPTY, null, message, 0L);
    }

    public Status getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public long getUpdateTime() {
        return updateTime;
    }
}
