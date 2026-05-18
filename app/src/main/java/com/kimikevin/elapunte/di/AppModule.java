package com.kimikevin.elapunte.di;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.kimikevin.elapunte.BuildConfig;
import com.kimikevin.elapunte.model.NoteDatabase;
import com.kimikevin.elapunte.model.dao.NoteDao;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    private static final String API_HOST = BuildConfig.API_HOST;
    private static final int API_PORT = 9090;

    @Provides
    @Singleton
    public NoteDatabase provideNoteDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, NoteDatabase.class, "note_database")
                .addMigrations(MIGRATION)
                .build();
    }

    static final Migration MIGRATION = new Migration(7,8) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE note_table ADD COLUMN pending_action TEXT");
        }
    };

    @Provides
    @Singleton
    public NoteDao provideNoteDao(NoteDatabase database) {
        return database.getNoteDao();
    }


    @Provides
    @Singleton
    @Named("gRPCExecutor")
    public ExecutorService provideGrpcExecutor() {
        return Executors.newFixedThreadPool(4);
    }

}
