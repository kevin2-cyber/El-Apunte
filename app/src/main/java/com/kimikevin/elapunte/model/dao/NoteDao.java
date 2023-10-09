package com.kimikevin.elapunte.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.kimikevin.elapunte.model.entity.Note;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes WHERE note_id ==:noteId")
    LiveData<Note> getNote(int noteId);

    @Query("SELECT * FROM note_table")
    LiveData<List<Note>> getAllNotes();
}
