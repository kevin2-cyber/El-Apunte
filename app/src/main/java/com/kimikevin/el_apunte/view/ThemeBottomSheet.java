package com.kimikevin.el_apunte.view;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kimikevin.el_apunte.R;
import com.kimikevin.el_apunte.databinding.ThemeBottomSheetLayoutBinding;

public class ThemeBottomSheet extends BottomSheetDialogFragment {
    ThemeBottomSheetLayoutBinding binding;
    public static final String TAG = "ModalBottomSheet";
    public static final String THEME_KEY = "theme_key";
    private static final String PREF_KEY = "pref";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.theme_bottom_sheet_layout, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = view.getContext().getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        int savedMode = sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setCheckedButton(savedMode);

        RadioGroup radioGroup = binding.radioGroup;

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            if (checkedId == R.id.light_theme) {
                selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.dark_theme) {
                selectedMode = AppCompatDelegate.MODE_NIGHT_YES;
            }

            editor.putInt(THEME_KEY, selectedMode).apply();
            AppCompatDelegate.setDefaultNightMode(selectedMode);
            dismiss(); // Optional to close bottom sheet after selection


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