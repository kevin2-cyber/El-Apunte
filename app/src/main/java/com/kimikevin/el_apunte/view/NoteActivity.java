package com.kimikevin.el_apunte.view;

import static com.kimikevin.el_apunte.view.ThemeBottomSheet.THEME_KEY;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.kimikevin.el_apunte.viewmodel.NoteViewModel;

import java.util.ArrayList;
import java.util.List;

public class NoteActivity extends AppCompatActivity {
    private ActivityNoteBinding binding;
    private NoteViewModel noteViewModel;
    NoteClickHandler handler;
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
        EdgeToEdge.enable(this);

        // Load saved theme preference and apply
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
        int savedMode = sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
        setContentView(R.layout.activity_note);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_note);
        handler = new NoteClickHandler(this);
        binding.setClickHandler(handler);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        noteViewModel.getAllNotes().observe(this, notes -> {
            noteList = (ArrayList<Note>) notes;

            loadRecyclerView();

        });

        binding.themeSwitch.setOnClickListener(view -> {
            ThemeBottomSheet themeBottomSheet = new ThemeBottomSheet();
            themeBottomSheet.show(getSupportFragmentManager(), ThemeBottomSheet.TAG);
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
        noteAdapter.setListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onNoteClick(Note note) {
                selectedNoteId = note.getId();
                Intent intent = new Intent(NoteActivity.this, EditActivity.class);

                intent.putExtra(EditActivity.NOTE_ID, selectedNoteId);
                intent.putExtra(EditActivity.NOTE_TITLE, note.getTitle());
                intent.putExtra(EditActivity.NOTE_CONTENT, note.getContent());

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
        Context context;

        public NoteClickHandler(Context context) {
            this.context = context;
        }

        public void onFabClick(View view) {
            Intent intent = new Intent(NoteActivity.this, EditActivity.class);
            startActivityIfNeeded(intent, ADD_NOTE_REQUEST_CODE);
        }

        public void onFilterClick(View view) {
            LinearLayoutManager manager = new LinearLayoutManager(NoteActivity.this, LinearLayoutManager.VERTICAL, true);
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

            noteViewModel.insertNote(note);
            Log.v(TAG, "Inserted " + note.getTitle());
        } else if (requestCode == EDIT_NOTE_REQUEST_CODE && resultCode == RESULT_OK) {
            Note note = new Note();
            note.setTitle(data.getStringExtra(EditActivity.NOTE_TITLE));
            note.setContent(data.getStringExtra(EditActivity.NOTE_CONTENT));

            note.setId(selectedNoteId);

            noteViewModel.updateNote(note);
            Log.v(TAG, "Updated " + note.getTitle());
        }
    }
}