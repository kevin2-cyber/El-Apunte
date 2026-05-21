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
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.kimikevin.elapunte.databinding.ActivityMainBinding;
import com.kimikevin.elapunte.util.TokenManager;
import com.kimikevin.elapunte.viewmodel.SplashViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Inject
    TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        applySavedTheme();

        SplashViewModel splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        splashScreen.setKeepOnScreenCondition(() -> {
            Boolean isLoading = splashViewModel.getLoadingStatus().getValue();
            return isLoading == null || !isLoading;
        });

        setContentView(binding.getRoot());

        // Skip login if the user already has a valid token
        if (savedInstanceState == null && tokenManager.isLoggedIn()) {
            NavHostFragment navHost = (NavHostFragment)
                    getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (navHost != null) {
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.loginFragment, true)
                        .build();
                navHost.getNavController().navigate(R.id.noteListFragment, null, options);
            }
        }

        // Handle forced logout triggered by the auth interceptor (token refresh failure)
        tokenManager.getLogoutEvent().observe(this, shouldLogout -> {
            if (shouldLogout == null || !shouldLogout) return;
            NavHostFragment navHost = (NavHostFragment)
                    getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
            if (navHost != null) {
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build();
                navHost.getNavController().navigate(R.id.loginFragment, null, options);
            }
        });
    }

    private void applySavedTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        int savedMode = sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }
}
