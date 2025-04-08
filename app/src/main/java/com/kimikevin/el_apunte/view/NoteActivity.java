package com.kimikevin.el_apunte.view;

import static com.kimikevin.el_apunte.model.util.AppConstants.ADD_NOTE_REQUEST_CODE;
import static com.kimikevin.el_apunte.model.util.AppConstants.EDIT_NOTE_REQUEST_CODE;
import static com.kimikevin.el_apunte.model.util.AppConstants.NOTE_LOG_TAG;
import static com.kimikevin.el_apunte.model.util.AppConstants.PREF_KEY;
import static com.kimikevin.el_apunte.model.util.AppConstants.THEME_KEY;
import static com.kimikevin.el_apunte.model.util.AppConstants.TAG;
import static com.kimikevin.el_apunte.view.EditActivity.NOTE_CONTENT;
import static com.kimikevin.el_apunte.view.EditActivity.NOTE_TIME;
import static com.kimikevin.el_apunte.view.EditActivity.NOTE_TITLE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kimikevin.el_apunte.R;
import com.kimikevin.el_apunte.databinding.ActivityNoteBinding;
import com.kimikevin.el_apunte.model.entity.Note;
import com.kimikevin.el_apunte.view.adapter.NoteAdapter;
import com.kimikevin.el_apunte.view.util.NoteDiffCallback;
import com.kimikevin.el_apunte.viewmodel.NoteViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoteActivity extends AppCompatActivity {
    private ActivityNoteBinding binding;
    private NoteViewModel noteViewModel;
    NoteClickHandler handler;

    private ArrayList<Note> noteList = new ArrayList<>();
    RecyclerView notesRecyclerView;
    NoteAdapter noteAdapter;

    public int selectedNoteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Load saved theme preference and apply
       applySavedTheme();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_note);
        handler = new NoteClickHandler(this);
        binding.setClickHandler(handler);
        binding.setLifecycleOwner(this);

        // setting up view model
        setupViewModel();

        binding.themeSwitch.setOnClickListener(view -> {
            ThemeBottomSheet themeBottomSheet = new ThemeBottomSheet();
            themeBottomSheet.show(getSupportFragmentManager(), TAG);
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

    private void applySavedTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        int savedMode = sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }

    private void setupViewModel() {
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        noteViewModel.getAllNotes().observe(this, notes -> {
//            noteList = (ArrayList<Note>) notes;
//
//            loadRecyclerView();
            if (notes == null || notes.isEmpty()) {
                showEmptyState();
            } else {
                updateNoteList(notes);
            }

        });
    }

    private void updateNoteList(List<Note> notes) {
        if (noteAdapter == null) {
            initRecyclerView(notes);
        } else {
            noteAdapter.submitList(notes);
        }
    }

    private void initRecyclerView(List<Note> notes) {
        noteAdapter = new NoteAdapter(new NoteDiffCallback(), note -> {
            selectedNoteId = note.getId();
            openEditActivity(note);
        });

        binding.rvNotes.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotes.setAdapter(noteAdapter);
        noteAdapter.submitList(notes);
    }

    private void openEditActivity(Note note) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.NOTE_ID, note.getId());
        intent.putExtra(NOTE_TITLE, note.getTitle());
        intent.putExtra(NOTE_CONTENT, note.getContent());
        startActivityIfNeeded(intent, EDIT_NOTE_REQUEST_CODE);
    }

    private void setupSearch() {
        binding.searchView.clearFocus();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterNotes(newText);
                return true;
            }
        });
    }

    private void filterNotes(String query) {
        List<Note> filtered = noteList.stream()
                .filter(n -> n.getTitle().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        noteAdapter.submitList(filtered);
        binding.emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void setupThemeSwitch() {
        binding.themeSwitch.setOnClickListener(v -> {
            ThemeBottomSheet bottomSheet = new ThemeBottomSheet();
            bottomSheet.setThemeChangeListener(this::recreate);
            bottomSheet.show(getSupportFragmentManager(), "theme_bottom_sheet");
        });
    }

    private void handleAddNoteResult(Intent data) {
        String title = data.getStringExtra(NOTE_TITLE);
        String content = data.getStringExtra(NOTE_CONTENT);

        if (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(content)) {
            Note note = new Note(title, content);
            noteViewModel.insertNote(note);
            Log.i(NOTE_LOG_TAG, "Note added: " + note.getTitle());
        }
    }

    private void handleEditNoteResult(Intent data) {
        if (selectedNoteId == -1) return;

        Note note = new Note(
                data.getStringExtra(NOTE_TITLE),
                data.getStringExtra(NOTE_CONTENT)
        );
        note.setId(selectedNoteId);
        noteViewModel.updateNote(note);
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
        noteAdapter.setListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onNoteClick(Note note) {
                selectedNoteId = note.getId();
                Intent intent = new Intent(NoteActivity.this, EditActivity.class);

                intent.putExtra(EditActivity.NOTE_ID, selectedNoteId);
                intent.putExtra(NOTE_TITLE, note.getTitle());
                intent.putExtra(NOTE_CONTENT, note.getContent());

                startActivityIfNeeded(intent, EDIT_NOTE_REQUEST_CODE);
            }

            @Override
            public void onNoteDelete(Note note) {
                noteViewModel.deleteNote(note);
                Toast.makeText(NoteActivity.this, "Note Deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class NoteClickHandler {
        private final Context context;

        public NoteClickHandler(Context context) {
            this.context = context;
        }

        public void onFabClick(View view) {
            startActivityIfNeeded(
                    new Intent(context, EditActivity.class),
                    ADD_NOTE_REQUEST_CODE
            );
        }

        public void onFilterClick(View view) {
            LinearLayoutManager manager = (LinearLayoutManager) binding.rvNotes.getLayoutManager();
            if (manager != null) {
                manager.setReverseLayout(!manager.getReverseLayout());
                manager.setStackFromEnd(!manager.getStackFromEnd());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            Note note = new Note();
            note.setTitle(data.getStringExtra(NOTE_TITLE));
            note.setContent(data.getStringExtra(NOTE_CONTENT));

            noteViewModel.insertNote(note);
            Log.i(NOTE_LOG_TAG, "Inserted " + note.getTitle() + " at " + note.getFormattedDate());
        } else if (requestCode == EDIT_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            Note note = new Note();
            note.setTitle(data.getStringExtra(NOTE_TITLE));
            note.setContent(data.getStringExtra(NOTE_CONTENT));
            note.setFormattedDate(data.getStringExtra(NOTE_TIME));

            note.setId(selectedNoteId);

            noteViewModel.updateNote(note);
            Log.i(NOTE_LOG_TAG, "Updated " + note.getTitle() + " at " + note.getFormattedDate());
        }
    }
}