package com.kimikevin.el_apunte.view.util;

import android.app.UiModeManager;
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
    private static final String RADIO_BUTTON_KEY = "radio";
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

        updateUI(sharedPreferences);

        RadioGroup radioGroup = binding.radioGroup;

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
           if (checkedId == R.id.light_theme) {
               editor.putBoolean(RADIO_BUTTON_KEY, true).apply();
               updateUI(sharedPreferences);
           } else {
               editor.putBoolean(RADIO_BUTTON_KEY, false).apply();
               updateUI(sharedPreferences);
           }
        });


    }

    private void updateUI(SharedPreferences sharedPreferences) {
        boolean isChecked = sharedPreferences.getBoolean(RADIO_BUTTON_KEY, false);
        binding.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.light_theme) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else if (checkedId == R.id.dark_theme) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
             else if (checkedId == R.id.system_theme) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
        });
    }
}
