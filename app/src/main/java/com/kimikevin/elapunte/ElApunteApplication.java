package com.kimikevin.elapunte;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import javax.inject.Inject;

import androidx.hilt.work.HiltWorkerFactory;

import java.util.concurrent.TimeUnit;

import dagger.hilt.android.HiltAndroidApp;
import io.grpc.ManagedChannel;

@HiltAndroidApp
public class ElApunteApplication extends Application implements Configuration.Provider {

    @Inject
    HiltWorkerFactory workerFactory;

    @Inject
    ManagedChannel grpcChannel;

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        if (grpcChannel != null && !grpcChannel.isShutdown()) {
            try {
                grpcChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                grpcChannel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
