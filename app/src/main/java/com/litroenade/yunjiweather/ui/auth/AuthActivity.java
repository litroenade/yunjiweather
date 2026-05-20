package com.litroenade.yunjiweather.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.litroenade.yunjiweather.MainActivity;
import com.litroenade.yunjiweather.auth.AuthSessionManager;
import com.litroenade.yunjiweather.auth.AuthSessionValidator;
import com.litroenade.yunjiweather.data.entity.UserEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.data.repository.AuthRepository;
import com.litroenade.yunjiweather.databinding.ActivityAuthBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private AuthSessionManager sessionManager;
    private AuthRepository authRepository;
    private final AuthFormController formController = new AuthFormController();
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new AuthSessionManager(this);
        if (sessionManager.isLoggedIn()) {
            validateExistingSession();
            return;
        }
        showAuthForm();
    }

    private void validateExistingSession() {
        diskExecutor.execute(() -> {
            AppDatabase database = AppDatabase.getInstance(this);
            AuthSessionValidator.Result result = new AuthSessionValidator(
                    sessionManager,
                    database.userDao()::findById
            ).validate();
            runOnUiThread(() -> {
                if (isDestroyed()) {
                    return;
                }
                if (result.isValid()) {
                    openMainActivity();
                    return;
                }
                showAuthForm();
            });
        });
    }

    private void showAuthForm() {
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        authRepository = new AuthRepository(AppDatabase.getInstance(this).userDao());

        binding.submitButton.setOnClickListener(view -> submit());
        binding.switchModeButton.setOnClickListener(view -> {
            formController.toggleMode();
            renderMode();
        });
        binding.passwordInput.setOnEditorActionListener(this::handlePasswordEditorAction);
        renderMode();
    }

    private void renderMode() {
        clearErrors();
        AuthFormController.FormState state = formController.getState();
        binding.modeTitle.setText(state.getTitle());
        binding.modeSubtitle.setText(state.getSubtitle());
        binding.authHelperText.setText(state.getHelperText());
        binding.displayNameInputLayout.setVisibility(state.isDisplayNameVisible() ? View.VISIBLE : View.GONE);
        binding.submitButton.setText(state.getSubmitText());
        binding.switchModeButton.setText(state.getSwitchText());
    }

    private void submit() {
        clearErrors();
        hideKeyboard();
        String username = readText(binding.usernameInput.getText());
        String password = readText(binding.passwordInput.getText());
        String displayName = readText(binding.displayNameInput.getText());
        AuthFormController.ValidationResult validation = formController.validateSubmit(username, password);
        if (!validation.isValid()) {
            showError(validation.getErrorMessage(), validation.getField());
            return;
        }
        setLoading(true);
        AuthFormController.Mode mode = formController.getMode();
        diskExecutor.execute(() -> {
            try {
                AuthRepository.AuthResult result = mode == AuthFormController.Mode.REGISTER
                        ? authRepository.register(username, password, displayName, System.currentTimeMillis())
                        : authRepository.login(username, password, System.currentTimeMillis());
                runOnUiThread(() -> handleAuthResult(result));
            } catch (RuntimeException exception) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError(buildUnexpectedErrorMessage(exception), AuthFormController.Field.NONE);
                });
            }
        });
    }

    private void handleAuthResult(AuthRepository.AuthResult result) {
        setLoading(false);
        if (!result.isSuccess()) {
            showError(result.getErrorMessage(), AuthFormController.Field.NONE);
            return;
        }
        UserEntity user = result.getUser();
        sessionManager.login(user.id, user.username, user.displayName);
        openMainActivity();
    }

    private void setLoading(boolean loading) {
        binding.usernameInput.setEnabled(!loading);
        binding.displayNameInput.setEnabled(!loading);
        binding.passwordInput.setEnabled(!loading);
        binding.submitButton.setEnabled(!loading);
        binding.switchModeButton.setEnabled(!loading);
        binding.submitButton.setText(loading ? "请稍候…" : formController.getState().getSubmitText());
    }

    private void showError(String message, AuthFormController.Field field) {
        if (field == AuthFormController.Field.USERNAME) {
            showInputError(binding.usernameInputLayout, message);
            binding.usernameInput.requestFocus();
        } else if (field == AuthFormController.Field.PASSWORD) {
            showInputError(binding.passwordInputLayout, message);
            binding.passwordInput.requestFocus();
        } else {
            binding.errorText.setText(message);
            binding.errorText.setVisibility(View.VISIBLE);
        }
    }

    private void showInputError(TextInputLayout layout, String message) {
        layout.setError(message);
        binding.errorText.setVisibility(View.GONE);
    }

    private void clearErrors() {
        binding.usernameInputLayout.setError(null);
        binding.passwordInputLayout.setError(null);
        binding.errorText.setVisibility(View.GONE);
    }

    private boolean handlePasswordEditorAction(TextView view, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            submit();
            return true;
        }
        return false;
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view == null) {
            view = binding.getRoot();
        }
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private static String readText(CharSequence text) {
        return text == null ? "" : text.toString().trim();
    }

    private static String buildUnexpectedErrorMessage(RuntimeException exception) {
        String detail = exception.getMessage();
        if (detail == null || detail.trim().isEmpty()) {
            detail = exception.getClass().getSimpleName();
        }
        return "账户操作失败，请稍后重试：" + detail;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        diskExecutor.shutdown();
    }
}
