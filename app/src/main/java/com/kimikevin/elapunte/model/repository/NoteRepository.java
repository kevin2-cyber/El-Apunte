package com.kimikevin.elapunte.model.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.kimikevin.elapunte.model.NoteDatabase;
import com.kimikevin.elapunte.model.dao.NoteDao;
import com.kimikevin.elapunte.model.entity.Note;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {
    private NoteDao noteDao;

    private LiveData<List<Note>> notes;

    public NoteRepository(Application application) {
        NoteDatabase noteDatabase = NoteDatabase.getInstance(application);
        noteDao = noteDatabase.getNoteDao();
    }

    public LiveData<List<Note>> getAllNotes() {
        notes = noteDao.getAllNotes();
        return notes;
    }

    public LiveData<List<Note>> getNoteById(int noteId) {
        return noteDao.getNoteById(noteId);
    }

    public void insertNote(Note note) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // inserting note
                noteDao.insert(note);
            }
        });
    }

    public void updateNote(Note note) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // updating note
                noteDao.update(note);
            }
        });
    }

    public void deleteNote(Note note) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // deleting note
                noteDao.delete(note);
            }
        });
    }
}
