package com.kimikevin.el_apunte.viewmodel;

import static com.kimikevin.el_apunte.util.AppConstants.NOTE_LOG_TAG;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kimikevin.el_apunte.model.entity.Note;
import com.kimikevin.el_apunte.model.repository.NoteRepository;
import com.kimikevin.el_apunte.util.TimeAgoUtil;

import java.util.List;

public class NoteViewModel extends AndroidViewModel {
    private final NoteRepository repository;
    private final LiveData<List<Note>> allNotes;

    public NoteViewModel(@NonNull Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insertNote(Note note) {
        try {
            long timestamp = System.currentTimeMillis();
            note.setFormattedDate(TimeAgoUtil.getTimeUsing24HourFormat(timestamp));
            repository.insertNote(note);
        } catch (Exception e) {
            Log.e(NOTE_LOG_TAG, "Error inserting note", e);
        }
    }

    public void updateNote(Note note) {
        try {
            repository.updateNote(note);
        } catch (Exception e) {
            Log.e(NOTE_LOG_TAG, "Error updating note", e);
        }
    }

    public void deleteNote(Note note) {
        try {
            repository.deleteNote(note);
        } catch (Exception e) {
            Log.e(NOTE_LOG_TAG, "Error deleting note", e);
        }
    }
}