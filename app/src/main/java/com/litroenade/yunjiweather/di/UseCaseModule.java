package com.litroenade.yunjiweather.di;

import com.litroenade.yunjiweather.data.local.WarningDao;
import com.litroenade.yunjiweather.data.repository.AlertRepository;
import com.litroenade.yunjiweather.data.repository.CityRepository;
import com.litroenade.yunjiweather.data.repository.LifeIndexRepository;
import com.litroenade.yunjiweather.data.repository.SettingsRepository;
import com.litroenade.yunjiweather.data.repository.WeatherRepository;
import com.litroenade.yunjiweather.domain.usecase.DispatchWarningNotificationsUseCase;
import com.litroenade.yunjiweather.domain.usecase.LoadHomeWeatherPageUseCase;
import com.litroenade.yunjiweather.domain.usecase.LoadLifeIndexUseCase;
import com.litroenade.yunjiweather.domain.usecase.RefreshAllCityWeatherCacheUseCase;
import com.litroenade.yunjiweather.domain.usecase.RefreshWeatherWidgetUseCase;
import com.litroenade.yunjiweather.domain.usecase.RefreshWarningsUseCase;
import com.litroenade.yunjiweather.domain.usecase.SendDailyWeatherReminderUseCase;
import com.litroenade.yunjiweather.notification.WarningNotificationDispatcher;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public final class UseCaseModule {

    @Provides
    public LoadLifeIndexUseCase provideLoadLifeIndexUseCase(
            CityRepository cityRepository,
            LifeIndexRepository lifeIndexRepository,
            WeatherRepository weatherRepository
    ) {
        return new LoadLifeIndexUseCase(cityRepository, lifeIndexRepository, weatherRepository);
    }

    @Provides
    public LoadHomeWeatherPageUseCase provideLoadHomeWeatherPageUseCase(
            CityRepository cityRepository,
            WeatherRepository weatherRepository,
            WarningDao warningDao
    ) {
        return new LoadHomeWeatherPageUseCase(cityRepository, weatherRepository, warningDao);
    }

    @Provides
    public RefreshAllCityWeatherCacheUseCase provideRefreshAllCityWeatherCacheUseCase(
            WeatherRepository weatherRepository
    ) {
        return new RefreshAllCityWeatherCacheUseCase(weatherRepository);
    }

    @Provides
    public RefreshWeatherWidgetUseCase provideRefreshWeatherWidgetUseCase(
            CityRepository cityRepository,
            WeatherRepository weatherRepository
    ) {
        return new RefreshWeatherWidgetUseCase(cityRepository, weatherRepository, System::currentTimeMillis);
    }

    @Provides
    public RefreshWarningsUseCase provideRefreshWarningsUseCase(
            CityRepository cityRepository,
            AlertRepository alertRepository,
            SettingsRepository settingsRepository
    ) {
        return new RefreshWarningsUseCase(cityRepository, alertRepository, settingsRepository);
    }

    @Provides
    public DispatchWarningNotificationsUseCase provideDispatchWarningNotificationsUseCase(
            CityRepository cityRepository,
            AlertRepository alertRepository,
            SettingsRepository settingsRepository,
            WarningNotificationDispatcher dispatcher
    ) {
        return new DispatchWarningNotificationsUseCase(cityRepository, alertRepository, settingsRepository, dispatcher);
    }

    @Provides
    public SendDailyWeatherReminderUseCase provideSendDailyWeatherReminderUseCase(
            CityRepository cityRepository,
            WeatherRepository weatherRepository,
            SettingsRepository settingsRepository
    ) {
        return new SendDailyWeatherReminderUseCase(cityRepository, weatherRepository, settingsRepository);
    }
}
