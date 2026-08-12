package com.kimikevin.elapunte.model.repository;

import static com.kimikevin.elapunte.util.AppConstants.THEME_KEY;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class ThemeRepository {
    private final RxDataStore<Preferences> dataStore;
    private final Preferences.Key<Integer> themeKey = PreferencesKeys.intKey(THEME_KEY);

    @Inject
    public ThemeRepository(RxDataStore<Preferences> dataStore) {
        this.dataStore = dataStore;
    }

    public Flowable<Integer> getThemeMode() {
        return dataStore.data().map(prefs -> {
            Integer mode = prefs.get(themeKey);
            return mode != null ? mode : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        });
    }

    public void setThemeMode(int mode) {
        dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(themeKey, mode);
            return Single.just(mutablePreferences);
        }).subscribe();
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
