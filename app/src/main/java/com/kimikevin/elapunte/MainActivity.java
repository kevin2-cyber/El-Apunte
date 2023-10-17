package com.kimikevin.elapunte;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.kimikevin.elapunte.databinding.ActivityMainBinding;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.view.EditActivity;
import com.kimikevin.elapunte.view.adapter.NoteAdapter;
import com.kimikevin.elapunte.viewmodel.MainActivityViewModel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;
    private MainClickHandler handler;
    public static final String TAG = "TAG";
    private ArrayList<Note> noteList;
    RecyclerView notesRecyclerView;
    NoteAdapter noteAdapter;

    // request codes
    public static final int ADD_NOTE_REQUEST_CODE = 1;
    public static final int EDIT_NOTE_REQUEST_CODE = 2;
    public int selectedNoteId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        handler = new MainClickHandler(this);
        binding.setClickHandler(handler);

        viewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                noteList = (ArrayList<Note>) notes;

                for (Note note : notes) {
                    Log.i(TAG, note.getTitle());
                }
                loadRecyclerView();
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
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setItemAnimator(new DefaultItemAnimator());
        notesRecyclerView.setHasFixedSize(true);

        noteAdapter = new NoteAdapter(this);
        notesRecyclerView.setAdapter(noteAdapter);

        noteAdapter.setNotes(noteList);
//        addTextListener();

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
            ArrayList<Note> filterList = new ArrayList<>();
            if (noteList.get(selectedNoteId).getDateTime() == LocalDateTime.now()) {
                noteList.add(selectedNoteId, filterList.get(selectedNoteId));
            }
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
        }
    }
}