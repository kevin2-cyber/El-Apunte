package com.kimikevin.elapunte;

import static com.kimikevin.elapunte.util.AppConstants.PREF_KEY;
import static com.kimikevin.elapunte.util.AppConstants.THEME_KEY;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import dagger.hilt.android.HiltAndroidApp;
import timber.log.Timber;

@HiltAndroidApp
public class ElApunteApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.Tree tree = new Timber.DebugTree();
            Timber.plant(tree);
        }
        applySavedTheme();
    }

    private void applySavedTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        int savedMode = sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }
}