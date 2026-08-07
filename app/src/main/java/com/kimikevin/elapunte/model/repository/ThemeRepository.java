package com.kimikevin.elapunte.model.repository;

import static com.kimikevin.elapunte.util.AppConstants.PREF_KEY;
import static com.kimikevin.elapunte.util.AppConstants.THEME_KEY;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class ThemeRepository {
    private final SharedPreferences sharedPreferences;

    @Inject
    public ThemeRepository(@ApplicationContext Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
    }

    public int getThemeMode() {
        return sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setThemeMode(int mode) {
        sharedPreferences.edit().putInt(THEME_KEY, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
