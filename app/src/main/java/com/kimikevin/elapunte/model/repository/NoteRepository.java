package com.kimikevin.elapunte.model.repository;


import androidx.lifecycle.LiveData;

import com.kimikevin.elapunte.model.dao.NoteDao;
import com.kimikevin.elapunte.model.entity.Note;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class NoteRepository {
    private NoteDao noteDao;

    private LiveData<List<Note>> notes;

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Inject
    public NoteRepository(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    public LiveData<List<Note>> getAllNotes() {
        notes = noteDao.getAllNotes();
        return notes;
    }

    public void insertNote(Note note) {

        executorService.execute(() -> {
            // inserting note
            noteDao.insert(note);
        });
    }

    public void updateNote(Note note) {

        executorService.execute(() -> {
            // updating note
            noteDao.update(note);
        });
    }

    public void deleteNote(Note note) {

        executorService.execute(() -> {
            // deleting note
            noteDao.delete(note);
        });
    }
}