package com.kimikevin.elapunte.model.network;

import com.google.gson.Gson;
import com.kimikevin.elapunte.util.TokenManager;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthInterceptor implements okhttp3.Interceptor {
    private final TokenManager tokenManager;
    private final OkHttpClient refreshClient;
    private final String baseUrl;
    private final Gson gson = new Gson();
    private final Object refreshLock = new Object();

    public AuthInterceptor(TokenManager tokenManager,
                           OkHttpClient refreshClient,
                           String baseUrl) {
        this.tokenManager = tokenManager;
        this.refreshClient = refreshClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // Auth endpoints (login / register / refresh) don't need a Bearer token.
        if (original.url().encodedPath().contains("/auth/")) {
            return chain.proceed(original);
        }

        // Proactively refresh if the stored token is already expired.
        String token = getValidToken();
        if (token == null) {
            // No valid token and refresh failed — force logout.
            tokenManager.clearTokens();
            tokenManager.signalLogout();
            return chain.proceed(original);
        }

        Response response = chain.proceed(withBearer(original, token));

        // Reactive 401 safety-net (covers clock skew / server-side revocation).
        if (response.code() != 401) {
            return response;
        }
        response.close();

        synchronized (refreshLock) {
            // Another thread may have refreshed while we waited on the lock.
            String current = tokenManager.getAccessToken();
            if (current != null && !current.equals(token)) {
                return chain.proceed(withBearer(original, current));
            }

            String newToken = doRefresh();
            if (newToken != null) {
                return chain.proceed(withBearer(original, newToken));
            }

            tokenManager.clearTokens();
            tokenManager.signalLogout();
            return chain.proceed(original);
        }
    }

    /**
     * Returns a valid access token, refreshing it first if it has expired.
     * Returns null if the refresh itself fails.
     */
    private String getValidToken() {
        if (!tokenManager.isAccessTokenExpired()) {
            return tokenManager.getAccessToken();
        }
        synchronized (refreshLock) {
            // Re-check inside the lock — another thread may have already refreshed.
            if (!tokenManager.isAccessTokenExpired()) {
                return tokenManager.getAccessToken();
            }
            return doRefresh();
        }
    }

    private Request withBearer(Request request, String token) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
    }

    private String doRefresh() {
        String refreshToken = tokenManager.getRefreshToken();
        if (refreshToken == null) return null;

        String json = gson.toJson(new RefreshRequest(refreshToken));
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(baseUrl + "auth/refresh")
                .post(body)
                .build();

        try (Response response = refreshClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return null;
            AuthResponse auth = gson.fromJson(response.body().string(), AuthResponse.class);
            if (auth == null || auth.getAccessToken() == null) return null;
            tokenManager.saveTokens(
                    auth.getAccessToken(),
                    auth.getRefreshToken(),
                    auth.getExpiresInSeconds()
            );
            return auth.getAccessToken();
        } catch (IOException e) {
            return null;
        }
    }
}
