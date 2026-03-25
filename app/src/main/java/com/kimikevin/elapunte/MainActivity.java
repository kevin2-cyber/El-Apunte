package com.kimikevin.elapunte;

import static com.kimikevin.elapunte.util.AppConstants.PREF_KEY;
import static com.kimikevin.elapunte.util.AppConstants.THEME_KEY;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;

import com.kimikevin.elapunte.viewmodel.SplashViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        applySavedTheme();

        SplashViewModel splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        splashScreen.setKeepOnScreenCondition(() -> {
            Boolean isLoading = splashViewModel.getLoadingStatus().getValue();
            return isLoading == null || !isLoading;
        });

        setContentView(R.layout.activity_main);
    }

    private void applySavedTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        int savedMode = sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }
}
