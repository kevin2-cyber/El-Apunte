package com.kimikevin.el_apunte.model;

import static com.kimikevin.el_apunte.model.util.AppConstants.NOTE_LOG_TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kimikevin.el_apunte.model.dao.NoteDao;
import com.kimikevin.el_apunte.model.entity.Note;
import com.kimikevin.el_apunte.model.util.TimeAgoUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class}, version = 4, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {
    public abstract NoteDao getNoteDao();

    // Singleton
    private static NoteDatabase instance;

    public static synchronized NoteDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            NoteDatabase.class,
                            "note_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}