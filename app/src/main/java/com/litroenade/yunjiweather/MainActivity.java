package com.litroenade.yunjiweather;

import android.os.Bundle;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
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
import com.litroenade.yunjiweather.utils.ThemeModeUtils;
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
        startupExecutor.execute(() -> {
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
                initializeMain(result.getUser());
            });
        });
    }

    private void initializeMain(UserEntity user) {
        applyDarkModeSetting();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        NotificationHelper.createWarningChannel(this);
        NotificationHelper.createDailyReminderChannel(this);
        scheduleWeatherAlertWorker(user.id);
        scheduleDailyWeatherWorker(user.id);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    private void applyDarkModeSetting() {
        int targetMode = ThemeModeUtils.resolveNightMode(
                new SettingsManager(this).isDarkModeEnabled(),
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_NO
        );
        if (ThemeModeUtils.shouldApplyNightMode(AppCompatDelegate.getDefaultNightMode(), targetMode)) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }
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
        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startupExecutor.shutdownNow();
    }
}
