package com.kimikevin.elapunte;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kimikevin.elapunte.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
    }
}