package com.litroenade.yunjiweather.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.litroenade.yunjiweather.domain.usecase.SendDailyWeatherReminderUseCase;
import com.litroenade.yunjiweather.notification.NotificationHelper;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class DailyWeatherWorker extends Worker {

    private final SendDailyWeatherReminderUseCase sendDailyWeatherReminderUseCase;

    @AssistedInject
    public DailyWeatherWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            SendDailyWeatherReminderUseCase sendDailyWeatherReminderUseCase
    ) {
        super(context, workerParams);
        this.sendDailyWeatherReminderUseCase = sendDailyWeatherReminderUseCase;
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        try {
            SendDailyWeatherReminderUseCase.Result result = sendDailyWeatherReminderUseCase.execute();
            if (result.getStatus() == SendDailyWeatherReminderUseCase.Status.READY) {
                NotificationHelper.showDailyWeatherNotification(context, result.getTitle(), result.getContent());
                return Result.success();
            }
            if (result.getStatus() == SendDailyWeatherReminderUseCase.Status.SKIPPED) {
                return Result.success();
            }
            return Result.retry();
        } catch (RuntimeException exception) {
            return Result.retry();
        }
    }
}
