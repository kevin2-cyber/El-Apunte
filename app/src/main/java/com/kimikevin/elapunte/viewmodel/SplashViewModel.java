package com.kimikevin.elapunte.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashViewModel extends ViewModel {
    private static final String TAG = "SplashViewModel";
    private final MutableLiveData<Boolean> isLoadingComplete = new MutableLiveData<>(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SplashViewModel() {
        // Use getViewModelScope equivalent via ViewModelScope or
        // at minimum a ViewModel-managed executor.
        // For a simple splash, post directly — no artificial delay needed.
        // If you have real data to load, load it here and post true when done.
        loadAppData();
    }

    private void loadAppData() {
        executor.execute(() -> {
            // Perform real startup tasks here (e.g. check auth token, preload config)
            // No Thread.sleep() — post true when REAL work is done
            isLoadingComplete.postValue(true);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }

    public MutableLiveData<Boolean> getLoadingStatus() {
        return isLoadingComplete;
    }
}