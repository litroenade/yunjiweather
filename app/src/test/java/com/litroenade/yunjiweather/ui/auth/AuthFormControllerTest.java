package com.litroenade.yunjiweather.ui.auth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthFormControllerTest {

    @Test
    public void initialState_usesLoginMode() {
        AuthFormController controller = new AuthFormController();

        AuthFormController.FormState state = controller.getState();

        assertEquals(AuthFormController.Mode.LOGIN, state.getMode());
        assertEquals("登录账户", state.getTitle());
        assertEquals("登录", state.getSubmitText());
        assertFalse(state.isDisplayNameVisible());
    }

    @Test
    public void toggleMode_switchesToRegisterMode() {
        AuthFormController controller = new AuthFormController();

        controller.toggleMode();
        AuthFormController.FormState state = controller.getState();

        assertEquals(AuthFormController.Mode.REGISTER, state.getMode());
        assertEquals("注册账户", state.getTitle());
        assertEquals("注册并进入", state.getSubmitText());
        assertEquals("已有账户？返回登录", state.getSwitchText());
        assertTrue(state.isDisplayNameVisible());
    }

    @Test
    public void validateSubmit_rejectsInvalidUsernameBeforeRepositoryCall() {
        AuthFormController controller = new AuthFormController();
        controller.toggleMode();

        AuthFormController.ValidationResult result = controller.validateSubmit("bad-name", "secret123");

        assertFalse(result.isValid());
        assertEquals(AuthFormController.Field.USERNAME, result.getField());
        assertEquals("用户名只能使用 3 到 20 位英文、数字或下划线", result.getErrorMessage());
    }

    @Test
    public void validateSubmit_rejectsShortPasswordBeforeRepositoryCall() {
        AuthFormController controller = new AuthFormController();

        AuthFormController.ValidationResult result = controller.validateSubmit("alice", "12345");

        assertFalse(result.isValid());
        assertEquals(AuthFormController.Field.PASSWORD, result.getField());
        assertEquals("密码长度必须为 6 到 32 个字符", result.getErrorMessage());
    }

    @Test
    public void validateSubmit_acceptsValidRegisterInput() {
        AuthFormController controller = new AuthFormController();
        controller.toggleMode();

        AuthFormController.ValidationResult result = controller.validateSubmit("alice_01", "secret123");

        assertTrue(result.isValid());
        assertEquals(AuthFormController.Field.NONE, result.getField());
        assertEquals("", result.getErrorMessage());
    }
}
