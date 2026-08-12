package com.kimikevin.elapunte.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.model.repository.NoteRepository;
import com.kimikevin.elapunte.model.repository.ThemeRepository;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NoteViewModel extends ViewModel {
    private final NoteRepository repository;
    private final ThemeRepository themeRepository;
    private final LiveData<List<Note>> allNotes;

    private final MutableLiveData<String> _searchQuery;
    private final MutableLiveData<Boolean> _isReverseLayout;
    private final MutableLiveData<Note> _currentNote = new MutableLiveData<>();
    public enum SaveStatus { EMPTY, NO_CHANGES, SUCCESS}
    private final MutableLiveData<SaveStatus> _saveStatus = new MutableLiveData<>();
    private final MediatorLiveData<List<Note>> _filteredNotes = new MediatorLiveData<>();

    @Inject
    public NoteViewModel(NoteRepository repository, ThemeRepository themeRepository, SavedStateHandle savedStateHandle) {
        this.repository = repository;
        this.themeRepository = themeRepository;
        allNotes = repository.getAllNotes();

        this._searchQuery = savedStateHandle.getLiveData("search_query", "");
        this._isReverseLayout = savedStateHandle.getLiveData("is_reverse", false);


        _filteredNotes.addSource(allNotes, notes -> performFilter(notes, _searchQuery.getValue()));
        _filteredNotes.addSource(_searchQuery, query -> performFilter(allNotes.getValue(), query));
    }

    private void performFilter(List<Note> notes, String query) {
        if (notes == null) return;
        if (query == null || query.isEmpty()) {
            _filteredNotes.setValue(notes);
            return;
        }

        List<Note> filtered = notes.stream()
                .filter(n -> n.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        n.getContent().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        _filteredNotes.setValue(filtered);
    }

    public void setSearchQuery(String query) {
        _searchQuery.setValue(query);
    }

    public LiveData<List<Note>> getFilteredNotes() {
        return _filteredNotes;
    }

    public void toggleLayoutOrder() {
        Boolean current = _isReverseLayout.getValue();
        _isReverseLayout.setValue(current == null ? false : !current);
    }

    public LiveData<Boolean> getIsReverseLayout() {
        return _isReverseLayout;
    }

    public LiveData<Note> getCurrentNote() {
        return _currentNote;
    }

    public void setCurrentNote(Note note) {
        _currentNote.setValue(note);
    }

    public LiveData<SaveStatus> getSaveStatus() {
        return _saveStatus;
    }

    public void saveNote(String originalTitle, String originalContent, boolean isEdit) {
        Note note = _currentNote.getValue();
        if (note == null) return;

        String title = note.getTitle() != null ? note.getTitle().trim() : "";
        String content = note.getContent() != null ? note.getContent().trim() : "";

        if (title.isEmpty() && content.isEmpty()) {
            _saveStatus.setValue(SaveStatus.EMPTY);
            return;
        }

        boolean hasChanges = !title.equals(originalTitle) || !content.equals(originalContent);
        if (!hasChanges) {
            _saveStatus.setValue(SaveStatus.NO_CHANGES);
            return;
        }

        if (isEdit) {
            repository.updateNote(note);
        } else {
            repository.insertNote(note);
        }

        _saveStatus.setValue(SaveStatus.SUCCESS);
    }

    public void resetSaveStatus() {
        _saveStatus.setValue(null);
    }

    public void deleteNote(Note note) {
      repository.deleteNote(note);
    }

    public LiveData<Integer> getThemeMode() {
        return LiveDataReactiveStreams.fromPublisher(themeRepository.getThemeMode());
    }

    public void setThemeMode(int mode) {
        themeRepository.setThemeMode(mode);
    }
}
