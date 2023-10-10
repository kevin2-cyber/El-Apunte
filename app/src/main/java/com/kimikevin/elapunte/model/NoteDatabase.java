package com.kimikevin.elapunte.model;

import android.content.Context;
import android.icu.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kimikevin.elapunte.model.dao.NoteDao;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.view.util.Converters;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
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
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // insert data when database is created
            initializeData();
        }
    };

    private static void initializeData() {
        NoteDao noteDao = instance.getNoteDao();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // notes
                Note noteOne = new Note();
                noteOne.setTitle("Health");
                noteOne.setContent("Hello My Friend");
                noteOne.createDateFormatted();

                noteDao.insert(noteOne);
            }
        });
    }
}
