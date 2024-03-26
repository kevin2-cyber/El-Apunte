package com.kimikevin.el_apunte;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.kimikevin.el_apunte.databinding.ActivityMainBinding;
import com.kimikevin.el_apunte.model.entity.Note;
import com.kimikevin.el_apunte.view.EditActivity;
import com.kimikevin.el_apunte.view.adapter.NoteAdapter;
import com.kimikevin.el_apunte.view.util.ThemeBottomSheet;
import com.kimikevin.el_apunte.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    MainClickHandler handler;
    public static final String TAG = "TAG";
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        handler = new MainClickHandler(this);
        binding.setClickHandler(handler);

        viewModel.getAllNotes().observe(this, notes -> {
            noteList = (ArrayList<Note>) notes;

//            noteList.addAll(notes);

            loadRecyclerView();
        });

        setSupportActionBar(binding.themeToolbar);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.themeSwitch) {
            ThemeBottomSheet themeBottomSheet = new ThemeBottomSheet();
            themeBottomSheet.show(getSupportFragmentManager(), ThemeBottomSheet.TAG);
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadRecyclerView() {
        notesRecyclerView = binding.rvNotes;
        LinearLayoutManager manager = new LinearLayoutManager(this);
        notesRecyclerView.setLayoutManager(manager);
        notesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        notesRecyclerView.setHasFixedSize(true);

        noteAdapter = new NoteAdapter(this);
        notesRecyclerView.setAdapter(noteAdapter);

        noteAdapter.setNotes(noteList);

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
        private Context context;

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
}