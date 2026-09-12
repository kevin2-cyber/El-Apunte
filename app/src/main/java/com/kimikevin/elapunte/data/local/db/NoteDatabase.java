package com.kimikevin.elapunte.data.local.db;


import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.kimikevin.elapunte.data.local.dao.NoteDao;
import com.kimikevin.elapunte.model.Note;
import com.kimikevin.elapunte.util.UUIDConverters;

@Database(entities = {Note.class}, version = 1)
@TypeConverters({UUIDConverters.class})
public abstract class NoteDatabase extends RoomDatabase {
    public abstract NoteDao getNoteDao();
}
