package com.litroenade.yunjiweather.data.repository;

import com.litroenade.yunjiweather.data.entity.UserEntity;
import com.litroenade.yunjiweather.data.local.UserDao;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthRepositoryTest {

    @Test
    public void register_validUser_savesNormalizedUsername() {
        FakeUserDao userDao = new FakeUserDao();
        AuthRepository repository = new AuthRepository(userDao);

        AuthRepository.AuthResult result = repository.register("Alice_01", "secret123", "Alice", 100L);

        assertTrue(result.isSuccess());
        assertEquals("alice_01", result.getUser().username);
        assertEquals("Alice", result.getUser().displayName);
        assertEquals(100L, result.getUser().createTime);
    }

    @Test
    public void register_duplicateUsername_failsCaseInsensitive() {
        FakeUserDao userDao = new FakeUserDao();
        AuthRepository repository = new AuthRepository(userDao);
        repository.register("Alice", "secret123", "Alice", 100L);

        AuthRepository.AuthResult result = repository.register("ALICE", "secret123", "Alice2", 200L);

        assertFalse(result.isSuccess());
        assertEquals("用户名已存在", result.getErrorMessage());
        assertEquals(1, userDao.users.size());
    }

    @Test
    public void login_correctPassword_updatesLastLoginTime() {
        FakeUserDao userDao = new FakeUserDao();
        AuthRepository repository = new AuthRepository(userDao);
        repository.register("Alice", "secret123", "Alice", 100L);

        AuthRepository.AuthResult result = repository.login("ALICE", "secret123", 300L);

        assertTrue(result.isSuccess());
        assertEquals("alice", result.getUser().username);
        assertEquals(300L, userDao.findByNormalizedUsername("alice").lastLoginTime);
    }

    @Test
    public void login_wrongPassword_fails() {
        FakeUserDao userDao = new FakeUserDao();
        AuthRepository repository = new AuthRepository(userDao);
        repository.register("Alice", "secret123", "Alice", 100L);

        AuthRepository.AuthResult result = repository.login("alice", "wrong123", 300L);

        assertFalse(result.isSuccess());
        assertEquals("用户名或密码错误", result.getErrorMessage());
    }

    private static final class FakeUserDao implements UserDao {
        private final List<UserEntity> users = new ArrayList<>();
        private long nextId = 1L;

        @Override
        public long insert(UserEntity user) {
            user.id = nextId++;
            users.add(user);
            return user.id;
        }

        @Override
        public UserEntity findByNormalizedUsername(String username) {
            String normalized = username.toLowerCase(Locale.US);
            for (UserEntity user : users) {
                if (user.username.equals(normalized)) {
                    return user;
                }
            }
            return null;
        }

        @Override
        public UserEntity findById(long id) {
            for (UserEntity user : users) {
                if (user.id == id) {
                    return user;
                }
            }
            return null;
        }

        @Override
        public void updateLastLoginTime(long id, long lastLoginTime) {
            UserEntity user = findById(id);
            if (user != null) {
                user.lastLoginTime = lastLoginTime;
            }
        }
    }
}
