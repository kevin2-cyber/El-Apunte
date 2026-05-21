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
import com.kimikevin.elapunte.databinding.FragmentRegisterBinding;
import com.kimikevin.elapunte.viewmodel.AuthViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupListeners();
        observeViewModel(view);
    }

    private void setupListeners() {
        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister();
                return true;
            }
            return false;
        });
        binding.tvGoLogin.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());
    }

    private void attemptRegister() {
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

        authViewModel.register(username, password);
    }

    private void observeViewModel(@NonNull View view) {
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnRegister.setEnabled(!loading);
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
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.loginFragment, true)
                        .build();
                Navigation.findNavController(view).navigate(R.id.noteListFragment, null, options);
            }
        });
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
