package com.kimikevin.elapunte.model.repository;

import androidx.lifecycle.LiveData;

import com.kimikevin.elapunte.model.dao.NoteDao;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.util.TimeAgoUtil;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import timber.log.Timber;

public class NoteRepository {
    private final NoteDao noteDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public NoteRepository(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    public LiveData<List<Note>> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public void insertNote(Note note) {
        executor.execute(() -> {
            long timestamp = System.currentTimeMillis();
            note.setTimestamp(timestamp);
            note.setFormattedDate(TimeAgoUtil.formatChatTimestamp(timestamp));
            Timber.tag("NoteRepository").d("Inserting note: %s at %s", note, timestamp);
            noteDao.insert(note);
        });
    }

    public void updateNote(Note note) {
        executor.execute(() -> {
            long timestamp = System.currentTimeMillis();
            note.setTimestamp(timestamp);
            note.setFormattedDate(TimeAgoUtil.formatChatTimestamp(timestamp));
            Timber.tag("NoteRepository").d("Updating note: %s at %s", note, timestamp);
            noteDao.update(note);
        });
    }

    public void deleteNote(Note note) {
        executor.execute(() -> {
            Timber.tag("NoteRepository").d("Deleting note: %s at %s", note, System.currentTimeMillis());
            noteDao.delete(note);
        });
    }
}
