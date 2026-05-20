package com.litroenade.yunjiweather.auth;

import android.content.Context;
import android.content.SharedPreferences;

public final class AuthSessionManager {

    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_DISPLAY_NAME = "display_name";

    public static final String SESSION_PREF_NAME = "yunji_weather_session";
    private static final long NO_USER_ID = -1L;

    private final SessionStorage storage;

    public AuthSessionManager(Context context) {
        this(new SharedPreferencesSessionStorage(
                context.getApplicationContext().getSharedPreferences(SESSION_PREF_NAME, Context.MODE_PRIVATE)
        ));
    }

    public AuthSessionManager(SessionStorage storage) {
        this.storage = storage;
    }

    public void login(long userId, String username, String displayName) {
        if (userId <= 0L) {
            throw new IllegalArgumentException("用户 ID 不合法");
        }
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        storage.putSession(userId, username, displayName == null || displayName.trim().isEmpty() ? username : displayName);
    }

    public void logout() {
        storage.clear();
    }

    public boolean isLoggedIn() {
        return storage.getLong(KEY_USER_ID, NO_USER_ID) > 0L;
    }

    public long requireUserId() {
        long userId = storage.getLong(KEY_USER_ID, NO_USER_ID);
        if (userId <= 0L) {
            throw new IllegalStateException("当前未登录，不能访问用户数据");
        }
        return userId;
    }

    public String getUsername() {
        return storage.getString(KEY_USERNAME, "");
    }

    public String getDisplayName() {
        String displayName = storage.getString(KEY_DISPLAY_NAME, "");
        return displayName == null || displayName.trim().isEmpty() ? getUsername() : displayName;
    }

    public interface SessionStorage {
        long getLong(String key, long defaultValue);

        String getString(String key, String defaultValue);

        void putLong(String key, long value);

        void putString(String key, String value);

        default void putSession(long userId, String username, String displayName) {
            putLong(KEY_USER_ID, userId);
            putString(KEY_USERNAME, username);
            putString(KEY_DISPLAY_NAME, displayName);
        }

        void clear();
    }

    private static final class SharedPreferencesSessionStorage implements SessionStorage {
        private final SharedPreferences preferences;

        private SharedPreferencesSessionStorage(SharedPreferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public long getLong(String key, long defaultValue) {
            return preferences.getLong(key, defaultValue);
        }

        @Override
        public String getString(String key, String defaultValue) {
            return preferences.getString(key, defaultValue);
        }

        @Override
        public void putLong(String key, long value) {
            preferences.edit().putLong(key, value).apply();
        }

        @Override
        public void putString(String key, String value) {
            preferences.edit().putString(key, value).apply();
        }

        @Override
        public void putSession(long userId, String username, String displayName) {
            preferences.edit()
                    .putLong(KEY_USER_ID, userId)
                    .putString(KEY_USERNAME, username)
                    .putString(KEY_DISPLAY_NAME, displayName)
                    .apply();
        }

        @Override
        public void clear() {
            preferences.edit().clear().apply();
        }
    }
}
