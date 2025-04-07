package com.kimikevin.el_apunte.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kimikevin.el_apunte.model.entity.Note;
import com.kimikevin.el_apunte.model.repository.NoteRepository;
import com.kimikevin.el_apunte.model.util.TimeAgoUtil;

import java.util.List;

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
        long currentTimestamp = System.currentTimeMillis();  // Get current time in milliseconds
        Log.d("NOTE_DEBUG", "Current timestamp: " + currentTimestamp);  // Log the timestamp

        String formattedDate = TimeAgoUtil.getTimeUsing24HourFormat(System.currentTimeMillis());
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
//        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());
//        String formattedDate = sdf.format(new Date());