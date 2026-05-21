package com.kimikevin.elapunte.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.kimikevin.elapunte.model.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> authSuccess = new MutableLiveData<>();

    @Inject
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void login(String username, String password) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        observe(authRepository.login(username, password));
    }

    public void register(String username, String password) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        observe(authRepository.register(username, password));
    }

    public void logout() {
        authRepository.logout();
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getAuthSuccess() { return authSuccess; }

    private void observe(LiveData<AuthRepository.AuthResult> source) {
        source.observeForever(new Observer<AuthRepository.AuthResult>() {
            @Override
            public void onChanged(AuthRepository.AuthResult result) {
                source.removeObserver(this);
                isLoading.postValue(false);
                if (result.isSuccess()) {
                    authSuccess.postValue(true);
                } else {
                    errorMessage.postValue(result.getError());
                }
            }
        });
    }
}
