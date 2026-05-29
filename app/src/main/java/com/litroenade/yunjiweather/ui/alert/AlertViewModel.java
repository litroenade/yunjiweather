package com.litroenade.yunjiweather.ui.alert;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.data.entity.WarningEntity;
import com.litroenade.yunjiweather.data.repository.AlertRepository;
import com.litroenade.yunjiweather.domain.usecase.RefreshWarningsUseCase;
import com.litroenade.yunjiweather.utils.WarningListUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AlertViewModel extends AndroidViewModel {

    private final AlertRepository alertRepository;
    private final RefreshWarningsUseCase refreshWarningsUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> alertStateText = new MutableLiveData<>();
    private final MutableLiveData<List<WarningEntity>> warnings = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>("");

    @Inject
    public AlertViewModel(
            @NonNull Application application,
            AlertRepository alertRepository,
            RefreshWarningsUseCase refreshWarningsUseCase
    ) {
        super(application);
        this.alertRepository = alertRepository;
        this.refreshWarningsUseCase = refreshWarningsUseCase;
        refreshState();
    }

    public LiveData<String> getAlertStateText() {
        return alertStateText;
    }

    public LiveData<List<WarningEntity>> getWarnings() {
        return warnings;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void refreshState() {
        loading.setValue(true);
        message.setValue("");
        executorService.execute(() -> {
            try {
                RefreshWarningsUseCase.Result result = refreshWarningsUseCase.execute(System.currentTimeMillis());
                warnings.postValue(result.getWarnings());
                alertStateText.postValue(result.getStateText());
            } catch (RuntimeException exception) {
                warnings.postValue(Collections.emptyList());
                alertStateText.postValue("Warning refresh failed. Reason: " + readableMessage(exception));
            } finally {
                loading.postValue(false);
            }
        });
    }

    public void markWarningRead(String warningId) {
        executorService.execute(() -> {
            List<WarningEntity> currentWarnings = warnings.getValue();
            WarningEntity targetWarning = findWarning(currentWarnings, warningId);
            if (targetWarning != null) {
                alertRepository.markRead(targetWarning.locationId, warningId);
                message.postValue("Marked warning as read: " + targetWarning.title);
            }
            if (currentWarnings != null) {
                warnings.postValue(WarningListUtils.markRead(currentWarnings, warningId));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }

    private WarningEntity findWarning(List<WarningEntity> warningList, String warningId) {
        if (warningList == null) {
            return null;
        }
        for (WarningEntity warning : warningList) {
            if (warning.warningId.equals(warningId)) {
                return warning;
            }
        }
        return null;
    }

    private String readableMessage(RuntimeException exception) {
        String reason = exception.getMessage();
        if (reason == null || reason.trim().isEmpty()) {
            return exception.getClass().getSimpleName();
        }
        return reason;
    }
}
