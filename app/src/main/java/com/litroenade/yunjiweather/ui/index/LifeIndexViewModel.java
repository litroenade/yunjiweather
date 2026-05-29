package com.litroenade.yunjiweather.ui.index;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.litroenade.yunjiweather.data.model.LifeIndexDefaults;
import com.litroenade.yunjiweather.data.model.LifeIndexItem;
import com.litroenade.yunjiweather.domain.usecase.LoadLifeIndexUseCase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LifeIndexViewModel extends AndroidViewModel {

    private final LoadLifeIndexUseCase loadLifeIndexUseCase;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<LifeIndexItem>> indexItems = new MutableLiveData<>();
    private final MutableLiveData<String> stateText = new MutableLiveData<>();

    @Inject
    public LifeIndexViewModel(@NonNull Application application, LoadLifeIndexUseCase loadLifeIndexUseCase) {
        super(application);
        this.loadLifeIndexUseCase = loadLifeIndexUseCase;
        refresh();
    }

    public LiveData<List<LifeIndexItem>> getIndexItems() {
        return indexItems;
    }

    public LiveData<String> getStateText() {
        return stateText;
    }

    public void refresh() {
        executorService.execute(() -> {
            try {
                LoadLifeIndexUseCase.Result result = loadLifeIndexUseCase.execute(System.currentTimeMillis());
                indexItems.postValue(result.getLoadResult().getItems());
                stateText.postValue(result.getStateText());
            } catch (RuntimeException exception) {
                publishLocalFallback("生活建议加载失败，已显示通用本地建议：" + readableMessage(exception));
            }
        });
    }

    private void publishLocalFallback(String message) {
        indexItems.postValue(LifeIndexDefaults.createFallbackItems());
        stateText.postValue(message);
    }

    private String readableMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }
}
