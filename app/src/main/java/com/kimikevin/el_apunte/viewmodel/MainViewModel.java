package com.kimikevin.el_apunte.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kimikevin.el_apunte.model.entity.Note;
import com.kimikevin.el_apunte.model.repository.NoteRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    // repository
    private final NoteRepository repository;

    // live data
    private LiveData<List<Note>> allNotes;
    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
    }

    public LiveData<List<Note>> getAllNotes() {
        allNotes = repository.getAllNotes();
        return allNotes;
    }

    public void insertNote(Note note) {
        repository.insertNote(note);
    }

    public void updateNote(Note note) {
        repository.updateNote(note);
    }

    public void deleteNote(Note note) {
        repository.deleteNote(note);
    }
}
