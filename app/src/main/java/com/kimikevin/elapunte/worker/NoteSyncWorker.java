package com.kimikevin.elapunte.worker;

import static com.kimikevin.elapunte.util.AppConstants.NOTE_LOG_TAG;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.kimikevin.elapunte.model.repository.NoteRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Named;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;

@HiltWorker
public class NoteSyncWorker extends Worker {

    private final NoteRepository repository;

    @AssistedInject
    public NoteSyncWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters params,
            NoteRepository repository) {
        super(context, params);
        this.repository = repository;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(NOTE_LOG_TAG, " Start pending sync");
        try {
            repository.syncPendingNotesSync();
            Log.d(NOTE_LOG_TAG, "Sync completed successfully");
            return Result.success();
        } catch (Exception e) {
            Log.e(NOTE_LOG_TAG, "Sync failed: " + e.getMessage(), e);
            return Result.retry();
        }
    }
}
