package com.kimikevin.elapunte.model;


import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kimikevin.elapunte.model.dao.NoteDao;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.util.Converters;

@Database(entities = {Note.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class NoteDatabase extends RoomDatabase {
    public abstract NoteDao getNoteDao();
}
