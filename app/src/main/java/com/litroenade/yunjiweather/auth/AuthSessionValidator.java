package com.litroenade.yunjiweather.auth;

import com.litroenade.yunjiweather.data.entity.UserEntity;

public final class AuthSessionValidator {

    private final AuthSessionManager sessionManager;
    private final UserLookup userLookup;

    public AuthSessionValidator(AuthSessionManager sessionManager, UserLookup userLookup) {
        this.sessionManager = sessionManager;
        this.userLookup = userLookup;
    }

    public Result validate() {
        if (!sessionManager.isLoggedIn()) {
            return Result.invalid();
        }
        UserEntity user = userLookup.findById(sessionManager.requireUserId());
        if (user == null) {
            sessionManager.logout();
            return Result.invalid();
        }
        return Result.valid(user);
    }

    public interface UserLookup {
        UserEntity findById(long userId);
    }

    public static final class Result {
        private final UserEntity user;

        private Result(UserEntity user) {
            this.user = user;
        }

        public static Result valid(UserEntity user) {
            return new Result(user);
        }

        public static Result invalid() {
            return new Result(null);
        }

        public boolean isValid() {
            return user != null;
        }

        public UserEntity getUser() {
            return user;
        }
    }
}
