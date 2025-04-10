package com.kimikevin.el_apunte.view;

import static com.kimikevin.el_apunte.util.AppConstants.ADD_NOTE_REQUEST_CODE;
import static com.kimikevin.el_apunte.util.AppConstants.EDIT_NOTE_REQUEST_CODE;
import static com.kimikevin.el_apunte.util.AppConstants.NOTE_LOG_TAG;
import static com.kimikevin.el_apunte.util.AppConstants.PREF_KEY;
import static com.kimikevin.el_apunte.util.AppConstants.THEME_KEY;
import static com.kimikevin.el_apunte.util.AppConstants.TAG;
import static com.kimikevin.el_apunte.view.EditActivity.NOTE_CONTENT;
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

import com.kimikevin.el_apunte.R;
import com.kimikevin.el_apunte.databinding.ActivityNoteBinding;
import com.kimikevin.el_apunte.model.entity.Note;
import com.kimikevin.el_apunte.view.adapter.NoteAdapter;
import com.kimikevin.el_apunte.viewmodel.NoteViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoteActivity extends AppCompatActivity {
    private ActivityNoteBinding binding;
    private NoteViewModel noteViewModel;
    private NoteClickHandler handler;
    private ArrayList<Note> noteList = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private int selectedNoteId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        applySavedTheme();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_note);
        handler = new NoteClickHandler(this);
        binding.setClickHandler(handler);
        binding.setLifecycleOwner(this);

        setupViewModel();
        setupUI();
    }

    private void applySavedTheme() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        int savedMode = sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
    }

    private void setupViewModel() {
        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, notes -> {
            if (notes == null || notes.isEmpty()) {
                showEmptyState();
            } else {
                updateNoteList(notes);
            }
        });
    }

    private void setupUI() {
        binding.themeSwitch.setOnClickListener(view -> {
            ThemeBottomSheet themeBottomSheet = new ThemeBottomSheet();
            themeBottomSheet.show(getSupportFragmentManager(), TAG);
        });

        setupSearchView();
        initRecyclerView();
    }

    private void setupSearchView() {
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

    private void initRecyclerView() {
        noteAdapter = new NoteAdapter(this);
        noteAdapter.setListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onNoteClick(Note note) {
                selectedNoteId = note.getId();
                openEditActivity(note);
            }

            @Override
            public void onNoteDelete(Note note) {
                noteViewModel.deleteNote(note);
                Toast.makeText(NoteActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.rvNotes.setLayoutManager(layoutManager);
        binding.rvNotes.setItemAnimator(new DefaultItemAnimator());
        binding.rvNotes.setAdapter(noteAdapter);
    }

    private void updateNoteList(List<Note> notes) {
        noteList = new ArrayList<>(notes);
        noteAdapter.submitList(noteList);
        binding.emptyState.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        noteList.clear();
        noteAdapter.submitList(noteList);
        binding.emptyState.setVisibility(View.VISIBLE);
    }

    private void filterNotes(String query) {
        try {
            List<Note> filtered = noteList.stream()
                    .filter(n -> TextUtils.isEmpty(query) ||
                            n.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                            n.getContent().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());

            noteAdapter.submitList(filtered);
            binding.emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            Log.e(NOTE_LOG_TAG, "Filter error", e);
        }
    }

    private void openEditActivity(Note note) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.NOTE_ID, note.getId());
        intent.putExtra(NOTE_TITLE, note.getTitle());
        intent.putExtra(NOTE_CONTENT, note.getContent());
        startActivityIfNeeded(intent, EDIT_NOTE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == ADD_NOTE_REQUEST_CODE) {
            handleAddNoteResult(data);
        } else if (requestCode == EDIT_NOTE_REQUEST_CODE) {
            handleEditNoteResult(data);
        }
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
        Log.i(NOTE_LOG_TAG, "Note updated: " + note.getTitle());
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
                boolean reverseLayout = !manager.getReverseLayout();
                manager.setReverseLayout(reverseLayout);
                manager.setStackFromEnd(reverseLayout);
            }
        }
    }
}