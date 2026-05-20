package com.litroenade.yunjiweather.ui.auth;

import com.litroenade.yunjiweather.auth.AuthPasswordUtils;
import com.litroenade.yunjiweather.data.repository.AuthRepository;

public final class AuthFormController {

    public enum Mode {
        LOGIN,
        REGISTER
    }

    public enum Field {
        USERNAME,
        PASSWORD,
        NONE
    }

    private Mode mode = Mode.LOGIN;

    public void toggleMode() {
        mode = mode == Mode.LOGIN ? Mode.REGISTER : Mode.LOGIN;
    }

    public Mode getMode() {
        return mode;
    }

    public FormState getState() {
        if (mode == Mode.REGISTER) {
            return new FormState(
                    mode,
                    "注册账户",
                    "创建本地账户后，城市、缓存和设置会独立保存",
                    "用户名 3-20 位，只支持英文、数字、下划线；密码 6-32 位",
                    "注册并进入",
                    "已有账户？返回登录",
                    true
            );
        }
        return new FormState(
                mode,
                "登录账户",
                "登录后进入你的专属天气空间",
                "使用已注册的本地账户登录；用户名不区分大小写",
                "登录",
                "没有账户？立即注册",
                false
        );
    }

    public ValidationResult validateSubmit(String username, String password) {
        try {
            AuthRepository.normalizeUsername(username);
        } catch (IllegalArgumentException exception) {
            return ValidationResult.invalid(Field.USERNAME, exception.getMessage());
        }
        try {
            AuthPasswordUtils.validatePassword(password);
            return ValidationResult.valid();
        } catch (IllegalArgumentException exception) {
            return ValidationResult.invalid(Field.PASSWORD, exception.getMessage());
        }
    }

    public static final class FormState {
        private final Mode mode;
        private final String title;
        private final String subtitle;
        private final String helperText;
        private final String submitText;
        private final String switchText;
        private final boolean displayNameVisible;

        private FormState(
                Mode mode,
                String title,
                String subtitle,
                String helperText,
                String submitText,
                String switchText,
                boolean displayNameVisible
        ) {
            this.mode = mode;
            this.title = title;
            this.subtitle = subtitle;
            this.helperText = helperText;
            this.submitText = submitText;
            this.switchText = switchText;
            this.displayNameVisible = displayNameVisible;
        }

        public Mode getMode() {
            return mode;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getHelperText() {
            return helperText;
        }

        public String getSubmitText() {
            return submitText;
        }

        public String getSwitchText() {
            return switchText;
        }

        public boolean isDisplayNameVisible() {
            return displayNameVisible;
        }
    }

    public static final class ValidationResult {
        private final boolean valid;
        private final Field field;
        private final String errorMessage;

        private ValidationResult(boolean valid, Field field, String errorMessage) {
            this.valid = valid;
            this.field = field;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, Field.NONE, "");
        }

        public static ValidationResult invalid(Field field, String errorMessage) {
            return new ValidationResult(false, field, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public Field getField() {
            return field;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
