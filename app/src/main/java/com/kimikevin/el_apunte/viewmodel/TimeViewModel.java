package com.kimikevin.el_apunte.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kimikevin.el_apunte.model.util.TimeAgoUtil;

import java.time.LocalDateTime;

public class TimeViewModel extends ViewModel {
    private final MutableLiveData<String> timeAgoLiveData = new MutableLiveData<>();

    public LiveData<String> getTimeAgoLiveData() {
        return timeAgoLiveData;
    }

    public void updateTimeAgo(LocalDateTime dateTime) {
        String timeAgo = TimeAgoUtil.getTimeAgo(dateTime);
        timeAgoLiveData.setValue(timeAgo);
    }
}
