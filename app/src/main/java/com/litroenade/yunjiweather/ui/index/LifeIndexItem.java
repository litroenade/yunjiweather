package com.litroenade.yunjiweather.ui.index;

public final class LifeIndexItem {

    private final String name;
    private final String level;
    private final String advice;
    private final String detail;

    public LifeIndexItem(String name, String level, String advice, String detail) {
        this.name = name;
        this.level = level;
        this.advice = advice;
        this.detail = detail;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    public String getAdvice() {
        return advice;
    }

    public String getDetail() {
        return detail;
    }
}
