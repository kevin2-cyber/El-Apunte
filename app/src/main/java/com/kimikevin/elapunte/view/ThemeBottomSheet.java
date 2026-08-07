package com.kimikevin.elapunte.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kimikevin.elapunte.R;
import com.kimikevin.elapunte.databinding.ThemeBottomSheetLayoutBinding;
import com.kimikevin.elapunte.viewmodel.NoteViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ThemeBottomSheet extends BottomSheetDialogFragment {
    ThemeBottomSheetLayoutBinding binding;
    NoteViewModel noteViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ThemeBottomSheetLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        int savedMode = noteViewModel.getThemeMode();
        setCheckedButton(savedMode);

        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            if (checkedId == R.id.light_theme) {
                selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.dark_theme) {
                selectedMode = AppCompatDelegate.MODE_NIGHT_YES;
            }

            noteViewModel.setThemeMode(selectedMode);
            dismiss();
        });
    }

    private void setCheckedButton(int mode) {
        if (mode == AppCompatDelegate.MODE_NIGHT_NO) {
            binding.radioGroup.check(R.id.light_theme);
        } else if (mode == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.radioGroup.check(R.id.dark_theme);
        } else {
            binding.radioGroup.check(R.id.system_theme);
        }
    }
}
