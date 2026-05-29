package com.litroenade.yunjiweather.data.model;

public final class CustomThemeCropAnchor {

    public static final String TOP = "top";
    public static final String CENTER = "center";
    public static final String BOTTOM = "bottom";

    private CustomThemeCropAnchor() {
    }

    public static String normalize(String cropAnchor) {
        if (TOP.equals(cropAnchor) || CENTER.equals(cropAnchor) || BOTTOM.equals(cropAnchor)) {
            return cropAnchor;
        }
        return CENTER;
    }
}
