package com.litroenade.yunjiweather.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.litroenade.yunjiweather.domain.usecase.RefreshWeatherWidgetUseCase;
import com.litroenade.yunjiweather.widget.WeatherAppWidgetProvider;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class WeatherWidgetRefreshWorker extends Worker {

    private final RefreshWeatherWidgetUseCase refreshWeatherWidgetUseCase;

    @AssistedInject
    public WeatherWidgetRefreshWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters workerParams,
            RefreshWeatherWidgetUseCase refreshWeatherWidgetUseCase
    ) {
        super(context, workerParams);
        this.refreshWeatherWidgetUseCase = refreshWeatherWidgetUseCase;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            RefreshWeatherWidgetUseCase.Result result = refreshWeatherWidgetUseCase.execute(false);
            WeatherAppWidgetProvider.updateAll(getApplicationContext());
            if (result.getStatus() == RefreshWeatherWidgetUseCase.Status.FAILED) {
                return Result.retry();
            }
            return Result.success();
        } catch (RuntimeException exception) {
            return Result.retry();
        }
    }
}
