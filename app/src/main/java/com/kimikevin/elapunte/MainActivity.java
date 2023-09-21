package com.kimikevin.elapunte;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import com.kimikevin.elapunte.databinding.ActivityMainBinding;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setNote(new Note());

        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
    }
}