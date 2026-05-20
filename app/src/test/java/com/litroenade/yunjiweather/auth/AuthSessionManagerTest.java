package com.litroenade.yunjiweather.auth;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthSessionManagerTest {

    @Test
    public void login_savesSession() {
        AuthSessionManager manager = new AuthSessionManager(new MemorySessionStorage());

        manager.login(7L, "alice", "Alice");

        assertTrue(manager.isLoggedIn());
        assertEquals(7L, manager.requireUserId());
        assertEquals("alice", manager.getUsername());
        assertEquals("Alice", manager.getDisplayName());
    }

    @Test
    public void logout_clearsSession() {
        AuthSessionManager manager = new AuthSessionManager(new MemorySessionStorage());
        manager.login(7L, "alice", "Alice");

        manager.logout();

        assertFalse(manager.isLoggedIn());
    }

    @Test(expected = IllegalStateException.class)
    public void requireUserId_throwsWhenNotLoggedIn() {
        AuthSessionManager manager = new AuthSessionManager(new MemorySessionStorage());

        manager.requireUserId();
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
