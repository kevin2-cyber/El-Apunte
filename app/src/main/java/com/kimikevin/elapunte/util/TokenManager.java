package com.kimikevin.elapunte.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

import static com.kimikevin.elapunte.util.AppConstants.ACCESS_TOKEN_EXPIRY_KEY;
import static com.kimikevin.elapunte.util.AppConstants.ACCESS_TOKEN_KEY;
import static com.kimikevin.elapunte.util.AppConstants.AUTH_PREF_KEY;
import static com.kimikevin.elapunte.util.AppConstants.REFRESH_TOKEN_KEY;

@Singleton
public class TokenManager {
    // Refresh 30 s before the token actually expires to avoid edge-case 401s.
    private static final long EXPIRY_BUFFER_MS = 30_000;

    private final SharedPreferences prefs;
    private final MutableLiveData<Boolean> logoutEvent = new MutableLiveData<>();

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        prefs = context.getSharedPreferences(AUTH_PREF_KEY, Context.MODE_PRIVATE);
    }

    public void saveTokens(String accessToken, String refreshToken, long expiresInSeconds) {
        long expiryMs = System.currentTimeMillis() + expiresInSeconds * 1_000;
        prefs.edit()
                .putString(ACCESS_TOKEN_KEY, accessToken)
                .putString(REFRESH_TOKEN_KEY, refreshToken)
                .putLong(ACCESS_TOKEN_EXPIRY_KEY, expiryMs)
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(ACCESS_TOKEN_KEY, null);
    }

    public String getRefreshToken() {
        return prefs.getString(REFRESH_TOKEN_KEY, null);
    }

    public boolean isAccessTokenExpired() {
        long expiry = prefs.getLong(ACCESS_TOKEN_EXPIRY_KEY, 0);
        // If expiry was never stored (old install / migration), treat as expired.
        if (expiry == 0) return true;
        return System.currentTimeMillis() >= expiry - EXPIRY_BUFFER_MS;
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    public void clearTokens() {
        prefs.edit().clear().apply();
    }

    public void signalLogout() {
        logoutEvent.postValue(true);
    }

    public LiveData<Boolean> getLogoutEvent() {
        return logoutEvent;
    }
}
