package com.litroenade.yunjiweather;

import android.content.res.Configuration;
import android.os.Bundle;
import android.content.Intent;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.litroenade.yunjiweather.auth.AuthSessionManager;
import com.litroenade.yunjiweather.auth.AuthSessionValidator;
import com.litroenade.yunjiweather.data.entity.UserEntity;
import com.litroenade.yunjiweather.data.local.AppDatabase;
import com.litroenade.yunjiweather.databinding.ActivityMainBinding;
import com.litroenade.yunjiweather.notification.NotificationHelper;
import com.litroenade.yunjiweather.settings.SettingsManager;
import com.litroenade.yunjiweather.ui.auth.AuthActivity;
import com.litroenade.yunjiweather.worker.DailyWeatherWorker;
import com.litroenade.yunjiweather.worker.WeatherAlertWorker;
import com.litroenade.yunjiweather.worker.WorkerScopeUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final ExecutorService startupExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthSessionManager sessionManager = new AuthSessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            returnToAuth();
            return;
        }
        showStartupView();
        startupExecutor.execute(() -> {
            try {
                AppDatabase database = AppDatabase.getInstance(this);
                AuthSessionValidator.Result result = new AuthSessionValidator(
                        sessionManager,
                        database.userDao()::findById
                ).validate();
                runOnUiThread(() -> {
                    if (isDestroyed()) {
                        return;
                    }
                    if (!result.isValid()) {
                        returnToAuth();
                        return;
                    }
                    if (applyUserDarkModeIfNeeded()) {
                        return;
                    }
                    initializeMain(result.getUser());
                });
            } catch (RuntimeException exception) {
                runOnUiThread(() -> {
                    if (isDestroyed()) {
                        return;
                    }
                    returnToAuth();
                });
            }
        });
    }

    private void initializeMain(UserEntity user) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NotificationHelper.createWarningChannel(this);
        NotificationHelper.createDailyReminderChannel(this);
        scheduleWeatherAlertWorker(user.id);
        scheduleDailyWeatherWorker(user.id);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        if (navHostFragment == null) {
            throw new IllegalStateException("主界面导航容器初始化失败");
        }
        // FragmentContainerView 在主题重建后不一定立即把 NavController 挂到 View tag，直接从 NavHostFragment 获取更稳定
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private boolean applyUserDarkModeIfNeeded() {
        boolean darkModeEnabled = new SettingsManager(this).isDarkModeEnabled();
        int targetMode = darkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean needsUiModeChange = darkModeEnabled
                ? currentNightMode != Configuration.UI_MODE_NIGHT_YES
                : currentNightMode == Configuration.UI_MODE_NIGHT_YES;
        if (AppCompatDelegate.getDefaultNightMode() == targetMode) {
            return false;
        }
        AppCompatDelegate.setDefaultNightMode(targetMode);
        return needsUiModeChange;
    }

    private void showStartupView() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(32, 32, 32, 32);
        container.setBackgroundResource(R.drawable.bg_app_soft);

        ProgressBar progressBar = new ProgressBar(this);
        container.addView(progressBar);

        TextView textView = new TextView(this);
        textView.setText("正在进入云迹天气…");
        textView.setTextSize(16f);
        textView.setTextColor(getColor(R.color.weather_text_primary));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = 24;
        container.addView(textView, textParams);

        setContentView(container);
    }

    private void scheduleWeatherAlertWorker(long ownerUserId) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                WeatherAlertWorker.class,
                6,
                TimeUnit.HOURS
        )
                .setInputData(new Data.Builder().putLong(WorkerScopeUtils.KEY_OWNER_USER_ID, ownerUserId).build())
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WorkerScopeUtils.weatherAlertWorkName(ownerUserId),
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    private void scheduleDailyWeatherWorker(long ownerUserId) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                DailyWeatherWorker.class,
                24,
                TimeUnit.HOURS
        )
                .setInputData(new Data.Builder().putLong(WorkerScopeUtils.KEY_OWNER_USER_ID, ownerUserId).build())
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WorkerScopeUtils.dailyWeatherWorkName(ownerUserId),
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    private void returnToAuth() {
        new AuthSessionManager(this).logout();
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startupExecutor.shutdownNow();
    }
}
