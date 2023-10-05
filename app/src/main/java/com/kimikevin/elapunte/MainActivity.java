package com.kimikevin.elapunte;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.kimikevin.elapunte.databinding.ActivityMainBinding;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.view.util.NoteUtil;
import com.kimikevin.elapunte.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;
    private MainClickHandler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setNote(new Note());

        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        handler = new MainClickHandler(this);
        binding.setClickHandler(handler);
    }

    public class MainClickHandler {
        private Context context;

        public MainClickHandler(Context context) {
            this.context = context;
        }

        public void onButtonClick(View view) {
            Toast.makeText(context, "Fab clicked", Toast.LENGTH_SHORT).show();
        }
    }
}