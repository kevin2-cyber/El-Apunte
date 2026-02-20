package com.kimikevin.elapunte.di;

import android.content.Context;

import androidx.room.Room;

import com.kimikevin.elapunte.BuildConfig;
import com.kimikevin.elapunte.model.NoteDatabase;
import com.kimikevin.elapunte.model.dao.NoteDao;
import com.kimikevin.elapunte.proto.NoteServiceGrpc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {
    private static final String GRPC_HOST = BuildConfig.GRPC_HOST;
    private static final int GRPC_PORT = 9090;

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

    @Provides
    @Singleton
    public NoteServiceGrpc.NoteServiceBlockingStub provideNoteServiceBlockingStub(ManagedChannel channel) {
        return NoteServiceGrpc.newBlockingStub(channel);
    }

    @Provides
    @Singleton
    public ManagedChannel provideGrpcChannel() {
        return OkHttpChannelBuilder
                .forAddress(GRPC_HOST, GRPC_PORT)
                .usePlaintext()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveTimeout(10, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .build();
    }

    @Provides
    @Singleton
    @Named("gRPCExecutor")
    public ExecutorService provideGrpcExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}
