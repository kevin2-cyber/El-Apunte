package com.kimikevin.elapunte.model;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.kimikevin.elapunte.model.dao.NoteDao;
import com.kimikevin.elapunte.model.entity.Note;

@Database(entities = {Note.class}, version = 4, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    public abstract NoteDao getNoteDao();
}