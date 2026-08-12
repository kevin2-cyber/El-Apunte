package com.kimikevin.elapunte.di;

import android.content.Context;

import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.rxjava3.RxDataStore;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.room.Room;

import com.kimikevin.elapunte.model.NoteDatabase;
import com.kimikevin.elapunte.model.dao.NoteDao;
import static com.kimikevin.elapunte.util.AppConstants.PREF_KEY;

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
                .fallbackToDestructiveMigration(true)
                .build();
    }

    @Provides
    @Singleton
    public NoteDao provideNoteDao(NoteDatabase database) {
        return database.getNoteDao();
    }

    @Provides
    @Singleton
    public RxDataStore<Preferences> provideDataStore(@ApplicationContext Context context) {
        return new RxPreferenceDataStoreBuilder(context, PREF_KEY).build();
    }
}
