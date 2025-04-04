package com.kimikevin.el_apunte;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kimikevin.el_apunte.databinding.ActivityMainBinding;
import com.kimikevin.el_apunte.model.entity.Note;
import com.kimikevin.el_apunte.view.EditActivity;
import com.kimikevin.el_apunte.view.adapter.NoteAdapter;
import com.kimikevin.el_apunte.viewmodel.MainViewModel;
import com.kimikevin.el_apunte.viewmodel.TimeViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private TimeViewModel timeViewModel;
    MainClickHandler handler;
    public static final String TAG = "TAG";

    private static final String SWITCH_BUTTON_KEY = "switch";
    private static final String PREF_KEY = "pref";
    private ArrayList<Note> noteList = new ArrayList<>();
    RecyclerView notesRecyclerView;
    NoteAdapter noteAdapter;

    // request codes
    public static final int ADD_NOTE_REQUEST_CODE = 1;
    public static final int EDIT_NOTE_REQUEST_CODE = 2;
    public int selectedNoteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_ElApunte);
        EdgeToEdge.enable(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSplashScreen().setOnExitAnimationListener(splashScreenView -> {
                final ObjectAnimator slideUp = ObjectAnimator.ofFloat(
                        splashScreenView,
                        View.TRANSLATION_Y,
                        0f,
                        -splashScreenView.getHeight()
                );
                slideUp.setInterpolator(new AnticipateInterpolator());
                slideUp.setDuration(500L);

                // Call SplashScreenView.remove at the end of your custom animation.
                slideUp.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        splashScreenView.remove();
                    }
                });

                // Run your animation.
                slideUp.start();
            });
        }
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        timeViewModel = new ViewModelProvider(this).get(TimeViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        handler = new MainClickHandler(this);
        binding.setClickHandler(handler);

        viewModel.getAllNotes().observe(this, notes -> {
            noteList = (ArrayList<Note>) notes;

//            noteList.addAll(notes);

            loadRecyclerView();
        });

        timeViewModel.getTimeAgoLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                //TODO: wrap timestamp in a view model
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        updateUI(sharedPreferences);

        binding.themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editor.putBoolean(SWITCH_BUTTON_KEY, true).apply();
                updateUI(sharedPreferences);
            } else {
                editor.putBoolean(SWITCH_BUTTON_KEY, false).apply();
                updateUI(sharedPreferences);
            }
        });

        binding.searchView.clearFocus();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

    private void filterList(String text) {
        List<Note> filteredList = new ArrayList<>();
        for (Note note: noteList) {
            if (note.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(note);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No notes", Toast.LENGTH_SHORT).show();
        } else {
            noteAdapter.setFilterList(filteredList);
        }
    }

    private void loadRecyclerView() {
        notesRecyclerView = binding.rvNotes;
        LinearLayoutManager manager = new LinearLayoutManager(this);
        notesRecyclerView.setLayoutManager(manager);
        notesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        notesRecyclerView.setHasFixedSize(true);

        noteAdapter = new NoteAdapter(this);


        noteAdapter.setNotes(noteList);

        notesRecyclerView.setAdapter(noteAdapter);

        // edit the note
        noteAdapter.setListener(note -> {
            selectedNoteId = note.getId();
            Intent intent = new Intent(MainActivity.this, EditActivity.class);

            intent.putExtra(EditActivity.NOTE_ID, selectedNoteId);
            intent.putExtra(EditActivity.NOTE_TITLE, note.getTitle());
            intent.putExtra(EditActivity.NOTE_CONTENT, note.getContent());

            startActivityIfNeeded(intent, EDIT_NOTE_REQUEST_CODE);
        });

        // delete a note
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Note noteToDelete = noteList.get(viewHolder.getAdapterPosition());
                viewModel.deleteNote(noteToDelete);
            }
        }).attachToRecyclerView(notesRecyclerView);
    }

    public class MainClickHandler {
        Context context;

        public MainClickHandler(Context context) {
            this.context = context;
        }

        public void onFabClick(View view) {
            Intent intent = new Intent(MainActivity.this, EditActivity.class);
            startActivityIfNeeded(intent,ADD_NOTE_REQUEST_CODE);
        }

        public void onFilterClick(View view) {
            LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, true);
            notesRecyclerView.setLayoutManager(manager);
            manager.setStackFromEnd(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            Note note = new Note();
            note.setTitle(data.getStringExtra(EditActivity.NOTE_TITLE));
            note.setContent(data.getStringExtra(EditActivity.NOTE_CONTENT));

            viewModel.insertNote(note);
            Log.v(TAG, "Inserted " + note.getTitle());
        } else if (requestCode == EDIT_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            Note note = new Note();
            note.setTitle(data.getStringExtra(EditActivity.NOTE_TITLE));
            note.setContent(data.getStringExtra(EditActivity.NOTE_CONTENT));

            note.setId(selectedNoteId);

            viewModel.updateNote(note);
            Log.v(TAG, "Updated " + note.getTitle());
        }
    }

    private void updateUI(SharedPreferences sharedPreferences) {
        boolean isChecked = sharedPreferences.getBoolean(SWITCH_BUTTON_KEY, false);
        binding.themeSwitch.setChecked(isChecked);
        if (isChecked) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}