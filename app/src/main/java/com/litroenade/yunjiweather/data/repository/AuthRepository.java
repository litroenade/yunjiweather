package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.auth.AuthPasswordUtils;
import com.litroenade.yunjiweather.data.entity.UserEntity;
import com.litroenade.yunjiweather.data.local.UserDao;

import java.util.Locale;
import java.util.regex.Pattern;

public final class AuthRepository {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    private final UserDao userDao;

    public AuthRepository(UserDao userDao) {
        this.userDao = userDao;
    }

    public AuthResult register(String username, String password, String displayName, long nowTime) {
        String normalizedUsername;
        try {
            normalizedUsername = normalizeUsername(username);
            AuthPasswordUtils.validatePassword(password);
        } catch (IllegalArgumentException exception) {
            return AuthResult.failure(exception.getMessage());
        }
        if (userDao.findByNormalizedUsername(normalizedUsername) != null) {
            return AuthResult.failure("用户名已存在");
        }
        String salt = AuthPasswordUtils.generateSalt();
        UserEntity user = new UserEntity(
                normalizedUsername,
                AuthPasswordUtils.hashPassword(password, salt),
                salt,
                normalizeDisplayName(displayName, normalizedUsername),
                nowTime,
                nowTime
        );
        user.id = userDao.insert(user);
        return AuthResult.success(user);
    }

    public AuthResult login(String username, String password, long nowTime) {
        String normalizedUsername;
        try {
            normalizedUsername = normalizeUsername(username);
            AuthPasswordUtils.validatePassword(password);
        } catch (IllegalArgumentException exception) {
            return AuthResult.failure("用户名或密码错误");
        }
        UserEntity user = userDao.findByNormalizedUsername(normalizedUsername);
        if (user == null || !AuthPasswordUtils.verifyPassword(password, user.passwordSalt, user.passwordHash)) {
            return AuthResult.failure("用户名或密码错误");
        }
        userDao.updateLastLoginTime(user.id, nowTime);
        user.lastLoginTime = nowTime;
        return AuthResult.success(user);
    }

    public static String normalizeUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        String normalized = username.trim().toLowerCase(Locale.US);
        if (!USERNAME_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("用户名只能使用 3 到 20 位英文、数字或下划线");
        }
        return normalized;
    }

    private static String normalizeDisplayName(String displayName, String username) {
        if (displayName == null || displayName.trim().isEmpty()) {
            return username;
        }
        return displayName.trim();
    }

    public static final class AuthResult {
        private final UserEntity user;
        private final String errorMessage;

        private AuthResult(UserEntity user, String errorMessage) {
            this.user = user;
            this.errorMessage = errorMessage;
        }

        public static AuthResult success(UserEntity user) {
            return new AuthResult(user, "");
        }

        public static AuthResult failure(String errorMessage) {
            return new AuthResult(null, errorMessage);
        }

        public boolean isSuccess() {
            return user != null;
        }

        public UserEntity getUser() {
            return user;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
