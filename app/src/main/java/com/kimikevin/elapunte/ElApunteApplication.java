package com.kimikevin.elapunte;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.kimikevin.elapunte.model.repository.ThemeRepository;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;

@HiltAndroidApp
public class ElApunteApplication extends Application {
    @Inject
    ThemeRepository themeRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
        AppCompatDelegate.setDefaultNightMode(themeRepository.getThemeMode());
    }
}
