package com.litroenade.yunjiweather.worker;

import com.litroenade.yunjiweather.auth.AuthSessionManager;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class WorkerScopeUtilsTest {

    @Test
    public void workNames_includeUserIdToAvoidCrossAccountReuse() {
        String firstUserAlertName = WorkerScopeUtils.weatherAlertWorkName(1L);
        String secondUserAlertName = WorkerScopeUtils.weatherAlertWorkName(2L);
        String firstUserDailyName = WorkerScopeUtils.dailyWeatherWorkName(1L);

        assertNotEquals(firstUserAlertName, secondUserAlertName);
        assertTrue(firstUserAlertName.contains("1"));
        assertTrue(secondUserAlertName.contains("2"));
        assertTrue(firstUserDailyName.contains("1"));
    }

    @Test
    public void shouldRunForCurrentUser_acceptsMatchingLoggedInUser() {
        AuthSessionManager sessionManager = new AuthSessionManager(new MemorySessionStorage());
        sessionManager.login(8L, "alice", "Alice");

        assertTrue(WorkerScopeUtils.shouldRunForCurrentUser(8L, sessionManager));
    }

    @Test
    public void shouldRunForCurrentUser_rejectsStaleUserOrMissingSession() {
        AuthSessionManager sessionManager = new AuthSessionManager(new MemorySessionStorage());
        sessionManager.login(8L, "alice", "Alice");

        assertFalse(WorkerScopeUtils.shouldRunForCurrentUser(9L, sessionManager));
        sessionManager.logout();
        assertFalse(WorkerScopeUtils.shouldRunForCurrentUser(8L, sessionManager));
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
