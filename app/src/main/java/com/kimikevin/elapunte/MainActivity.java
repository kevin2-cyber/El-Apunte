package com.kimikevin.elapunte;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;

import com.kimikevin.elapunte.view.NoteActivity;
import com.kimikevin.elapunte.viewmodel.SplashViewModel;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private SplashViewModel splashViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);
        EdgeToEdge.enable(this);

        splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);

        // Keep the splash screen on until the loading is complete
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> {
            Boolean isLoading = splashViewModel.getLoadingStatus().getValue();
            return isLoading == null || !isLoading;
        });

        // Observe the loading status to know when to transition
        splashViewModel.getLoadingStatus().observe(this, isLoadingComplete -> {
            if (Boolean.TRUE.equals(isLoadingComplete)) {
                // Start the next activity or update the UI
                proceedToMainContent();
            }
        });
    }

    private void proceedToMainContent() {
        Intent intent = new Intent(this, NoteActivity.class);
        startActivity(intent);
        finish();
    }
}