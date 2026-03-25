package com.kimikevin.elapunte;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import javax.inject.Inject;

import androidx.hilt.work.HiltWorkerFactory;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class ElApunteApplication extends Application implements Configuration.Provider {

    @Inject
    HiltWorkerFactory workerFactory;

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }
}
