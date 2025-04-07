package com.kimikevin.el_apunte.viewmodel;

import android.app.Application;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kimikevin.el_apunte.model.entity.Note;
import com.kimikevin.el_apunte.model.repository.NoteRepository;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteViewModel extends AndroidViewModel {
    // repository
    private final NoteRepository repository;

    // live data
    private LiveData<List<Note>> allNotes;
    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
    }

    public LiveData<List<Note>> getAllNotes() {
        allNotes = repository.getAllNotes();
        return allNotes;
    }

    public void insertNote(Note note) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());
        String formattedDate = sdf.format(new Date());

        note.setFormattedDate(formattedDate);
        Log.d("NOTE_DEBUG", "Saved date: " + note.getFormattedDate());
        repository.insertNote(note);
    }

    public void updateNote(Note note) {
        repository.updateNote(note);
    }

    public void deleteNote(Note note) {
        repository.deleteNote(note);
    }
}