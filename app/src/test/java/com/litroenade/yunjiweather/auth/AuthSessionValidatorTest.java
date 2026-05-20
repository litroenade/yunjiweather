package com.litroenade.yunjiweather.auth;

import com.litroenade.yunjiweather.data.entity.UserEntity;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthSessionValidatorTest {

    @Test
    public void validate_existingSessionAndUser_returnsValidUser() {
        MemorySessionStorage storage = new MemorySessionStorage();
        AuthSessionManager sessionManager = new AuthSessionManager(storage);
        sessionManager.login(3L, "alice", "Alice");
        AuthSessionValidator validator = new AuthSessionValidator(sessionManager, userId -> user(userId, "alice"));

        AuthSessionValidator.Result result = validator.validate();

        assertTrue(result.isValid());
        assertEquals(3L, result.getUser().id);
        assertTrue(sessionManager.isLoggedIn());
    }

    @Test
    public void validate_missingUser_clearsStaleSession() {
        MemorySessionStorage storage = new MemorySessionStorage();
        AuthSessionManager sessionManager = new AuthSessionManager(storage);
        sessionManager.login(9L, "old_user", "Old");
        AuthSessionValidator validator = new AuthSessionValidator(sessionManager, userId -> null);

        AuthSessionValidator.Result result = validator.validate();

        assertFalse(result.isValid());
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    public void validate_withoutSession_returnsInvalidWithoutLookup() {
        AuthSessionManager sessionManager = new AuthSessionManager(new MemorySessionStorage());
        AuthSessionValidator validator = new AuthSessionValidator(sessionManager, userId -> {
            throw new AssertionError("未登录状态不应该查询数据库");
        });

        AuthSessionValidator.Result result = validator.validate();

        assertFalse(result.isValid());
    }

    private static UserEntity user(long id, String username) {
        UserEntity user = new UserEntity(username, "hash", "salt", username, 100L, 100L);
        user.id = id;
        return user;
    }

    private static final class MemorySessionStorage implements AuthSessionManager.SessionStorage {
        private final Map<String, String> values = new HashMap<>();

        @Override
        public long getLong(String key, long defaultValue) {
            String value = values.get(key);
            return value == null ? defaultValue : Long.parseLong(value);
        }

        @Override
        public String getString(String key, String defaultValue) {
            String value = values.get(key);
            return value == null ? defaultValue : value;
        }

        @Override
        public void putLong(String key, long value) {
            values.put(key, Long.toString(value));
        }

        @Override
        public void putString(String key, String value) {
            values.put(key, value);
        }

        @Override
        public void clear() {
            values.clear();
        }
    }
}
