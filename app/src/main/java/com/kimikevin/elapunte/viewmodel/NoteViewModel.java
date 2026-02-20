package com.kimikevin.elapunte.viewmodel;

import static com.kimikevin.elapunte.util.AppConstants.NOTE_LOG_TAG;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.model.repository.NoteRepository;
import com.kimikevin.elapunte.util.TimeAgoUtil;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NoteViewModel extends ViewModel {
    private final NoteRepository repository;

    private final LiveData<List<Note>> allNotes;

    @Inject
    public NoteViewModel(NoteRepository repository) {
        this.repository = repository;
        allNotes = repository.getAllNotes();

        repository.syncNotes();
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