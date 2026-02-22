package com.kimikevin.elapunte.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.kimikevin.elapunte.model.entity.Note;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM note_table WHERE pending_action != 'DELETE' OR pending_action IS NULL ORDER BY timestamp DESC")
    LiveData<List<Note>> getAllNotes();

    @Query("SELECT * FROM note_table WHERE isSynced = 0 AND pending_action IS NOT NULL")
    List<Note> getUnsyncedNotes();

    @Query("UPDATE note_table SET isSynced = 1, pending_action = NULL WHERE note_id = :noteId")
    void markAsSynced(String noteId);

    @Query("SELECT COUNT(*) FROM note_table")
    int getCount();
}