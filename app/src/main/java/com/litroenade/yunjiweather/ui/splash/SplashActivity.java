package com.litroenade.yunjiweather.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.litroenade.yunjiweather.MainActivity;
import com.litroenade.yunjiweather.auth.AuthSessionManager;
import com.litroenade.yunjiweather.auth.AuthSessionValidator;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.databinding.ActivitySplashBinding;
import com.litroenade.yunjiweather.ui.auth.AuthActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long ROUTE_DELAY_MILLIS = 1100L;

    private ActivitySplashBinding binding;
    private AuthSessionManager sessionManager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sessionManager = new AuthSessionManager(this);
        playIntroAnimation();
        handler.postDelayed(this::routeNext, ROUTE_DELAY_MILLIS);
    }

    private void playIntroAnimation() {
        binding.splashLogoImage.setScaleX(0.78f);
        binding.splashLogoImage.setScaleY(0.78f);
        binding.splashLogoImage.setAlpha(0f);
        binding.splashTitleText.setTranslationY(24f);
        binding.splashTitleText.setAlpha(0f);
        binding.splashSubtitleText.setTranslationY(24f);
        binding.splashSubtitleText.setAlpha(0f);

        binding.splashLogoImage.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(520L)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        binding.splashTitleText.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(220L)
                .setDuration(420L)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
        binding.splashSubtitleText.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(360L)
                .setDuration(420L)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void routeNext() {
        if (!sessionManager.isLoggedIn()) {
            openAuthActivity();
            return;
        }
        executorService.execute(() -> {
            boolean validSession;
            try {
                AppDatabase database = AppDatabase.getInstance(this);
                AuthSessionValidator.Result result = new AuthSessionValidator(
                        sessionManager,
                        database.userDao()::findById
                ).validate();
                validSession = result.isValid();
            } catch (RuntimeException exception) {
                sessionManager.logout();
                validSession = false;
            }
            boolean finalValidSession = validSession;
            runOnUiThread(() -> {
                if (isDestroyed()) {
                    return;
                }
                if (finalValidSession) {
                    openMainActivity();
                } else {
                    openAuthActivity();
                }
            });
        });
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openAuthActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        executorService.shutdownNow();
        super.onDestroy();
    }
}
