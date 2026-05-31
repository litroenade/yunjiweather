package com.litroenade.yunjiweather.data.model;

public final class CustomThemeAsset {

    public static final String MEDIA_IMAGE = "image";
    public static final String MEDIA_GIF = "gif";

    private static final CustomThemeAsset EMPTY = new CustomThemeAsset(
            "",
            "",
            MEDIA_IMAGE,
            CustomThemeCropAnchor.CENTER,
            "",
            true
    );

    private final String id;
    private final String uri;
    private final String mediaType;
    private final String cropAnchor;
    private final String label;

    public CustomThemeAsset(
            String id,
            String uri,
            String mediaType,
            String cropAnchor,
            String label
    ) {
        this(id, uri, mediaType, cropAnchor, label, false);
    }

    private CustomThemeAsset(
            String id,
            String uri,
            String mediaType,
            String cropAnchor,
            String label,
            boolean allowEmpty
    ) {
        this.id = normalizeRequired(id, "asset id", allowEmpty);
        this.uri = normalizeRequired(uri, "asset uri", allowEmpty);
        this.mediaType = normalizeMediaType(mediaType);
        this.cropAnchor = CustomThemeCropAnchor.normalize(cropAnchor);
        this.label = label == null ? "" : label.trim();
    }

    public static CustomThemeAsset empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return uri.isEmpty();
    }

    public String getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getCropAnchor() {
        return cropAnchor;
    }

    public String getLabel() {
        return label;
    }

    public static String normalizeMediaType(String mediaType) {
        String normalized = mediaType == null ? "" : mediaType.trim().toLowerCase();
        return MEDIA_GIF.equals(normalized) ? MEDIA_GIF : MEDIA_IMAGE;
    }

    private static String normalizeRequired(String value, String fieldName, boolean allowEmpty) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty() && !allowEmpty) {
            throw new IllegalArgumentException("Custom theme " + fieldName + " must not be blank");
        }
        return normalized;
    }
}
