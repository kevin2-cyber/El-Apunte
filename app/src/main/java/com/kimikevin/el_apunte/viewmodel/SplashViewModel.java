package com.kimikevin.el_apunte.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.logging.Logger;

public class SplashViewModel extends ViewModel {
    private final MutableLiveData<Boolean> isLoadingComplete = new MutableLiveData<>(false);
    private final Logger logger = Logger.getLogger(SplashViewModel.class.getName());

    public SplashViewModel() {
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                isLoadingComplete.postValue(true);
            } catch (InterruptedException e) {
                logger.info(e.toString());
            }
        }).start();
    }

    public MutableLiveData<Boolean> getLoadingStatus() {
        return isLoadingComplete;
    }
}