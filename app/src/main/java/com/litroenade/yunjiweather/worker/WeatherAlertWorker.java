package com.litroenade.yunjiweather.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.litroenade.yunjiweather.domain.usecase.DispatchWarningNotificationsUseCase;
import com.litroenade.yunjiweather.notification.SystemWarningNotifier;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class WeatherAlertWorker extends Worker {

    private final DispatchWarningNotificationsUseCase dispatchWarningNotificationsUseCase;

    @AssistedInject
    public WeatherAlertWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            DispatchWarningNotificationsUseCase dispatchWarningNotificationsUseCase
    ) {
        super(context, workerParams);
        this.dispatchWarningNotificationsUseCase = dispatchWarningNotificationsUseCase;
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        try {
            dispatchWarningNotificationsUseCase.execute(new SystemWarningNotifier(context));
            return Result.success();
        } catch (RuntimeException exception) {
            return Result.retry();
        }
    }
}
