package com.kimikevin.elapunte.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.model.repository.NoteRepository;

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
    }

    public LiveData<List<Note>> getAllNotes() {
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
