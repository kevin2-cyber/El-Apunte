package com.kimikevin.elapunte.model.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.kimikevin.elapunte.model.dao.NoteDao;
import com.kimikevin.elapunte.model.network.AuthApi;
import com.kimikevin.elapunte.model.network.AuthRequest;
import com.kimikevin.elapunte.model.network.AuthResponse;
import com.kimikevin.elapunte.util.TokenManager;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import retrofit2.Response;

@Singleton
public class AuthRepository {
    private final AuthApi authApi;
    private final TokenManager tokenManager;
    private final NoteDao noteDao;
    private final ExecutorService executor;

    @Inject
    public AuthRepository(AuthApi authApi,
                          TokenManager tokenManager,
                          NoteDao noteDao,
                          @Named("networkExecutor") ExecutorService executor) {
        this.authApi = authApi;
        this.tokenManager = tokenManager;
        this.noteDao = noteDao;
        this.executor = executor;
    }

    public LiveData<AuthResult> login(String username, String password) {
        return executeAuth(authApi.login(new AuthRequest(username, password)));
    }

    public LiveData<AuthResult> register(String username, String password) {
        return executeAuth(authApi.register(new AuthRequest(username, password)));
    }

    public void logout() {
        executor.execute(() -> {
            tokenManager.clearTokens();
            noteDao.clearAll();
        });
    }

    private LiveData<AuthResult> executeAuth(retrofit2.Call<AuthResponse> call) {
        MutableLiveData<AuthResult> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                Response<AuthResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    tokenManager.saveTokens(
                            auth.getAccessToken(),
                            auth.getRefreshToken(),
                            auth.getExpiresInSeconds()
                    );
                    result.postValue(AuthResult.success());
                } else {
                    result.postValue(AuthResult.error(parseError(response.code())));
                }
            } catch (Exception e) {
                result.postValue(AuthResult.error("Network error. Check your connection."));
            }
        });
        return result;
    }

    private String parseError(int code) {
        switch (code) {
            case 401: return "Invalid username or password.";
            case 409: return "Username already taken.";
            case 400: return "Invalid request. Check your input.";
            default:  return "Server error (" + code + "). Try again.";
        }
    }

    public static class AuthResult {
        private final boolean success;
        private final String error;

        private AuthResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }

        public static AuthResult success() { return new AuthResult(true, null); }
        public static AuthResult error(String msg) { return new AuthResult(false, msg); }

        public boolean isSuccess() { return success; }
        public String getError() { return error; }
    }
}
