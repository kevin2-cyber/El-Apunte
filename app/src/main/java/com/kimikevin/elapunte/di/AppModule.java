package com.kimikevin.elapunte.di;

import android.content.Context;

import androidx.room.Room;

import com.kimikevin.elapunte.model.NoteDatabase;
import com.kimikevin.elapunte.model.dao.NoteDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public NoteDatabase provideNoteDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, NoteDatabase.class, "note_database")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public NoteDao provideNoteDao(NoteDatabase database) {
        return database.getNoteDao();
    }
}
