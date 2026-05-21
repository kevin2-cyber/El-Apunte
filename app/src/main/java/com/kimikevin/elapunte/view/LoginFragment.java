package com.kimikevin.elapunte.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.kimikevin.elapunte.R;
import com.kimikevin.elapunte.databinding.FragmentLoginBinding;
import com.kimikevin.elapunte.util.TokenManager;
import com.kimikevin.elapunte.viewmodel.AuthViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;

    @Inject
    TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (tokenManager.isLoggedIn()) {
            navigateToNotes(view);
            return;
        }

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupListeners();
        observeViewModel(view);
    }

    private void setupListeners() {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
        binding.tvGoRegister.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment));
    }

    private void attemptLogin() {
        String username = getText(binding.etUsername);
        String password = getText(binding.etPassword);

        if (username.isEmpty()) {
            binding.tilUsername.setError(getString(R.string.error_field_required));
            return;
        }
        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.error_field_required));
            return;
        }
        binding.tilUsername.setError(null);
        binding.tilPassword.setError(null);

        authViewModel.login(username, password);
    }

    private void observeViewModel(@NonNull View view) {
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!loading);
        });

        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.tvError.setText(error);
                binding.tvError.setVisibility(View.VISIBLE);
            } else {
                binding.tvError.setVisibility(View.GONE);
            }
        });

        authViewModel.getAuthSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                navigateToNotes(view);
            }
        });
    }

    private void navigateToNotes(@NonNull View view) {
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true)
                .build();
        Navigation.findNavController(view).navigate(R.id.noteListFragment, null, options);
    }

    private String getText(android.widget.EditText et) {
        CharSequence text = et.getText();
        return text != null ? text.toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
