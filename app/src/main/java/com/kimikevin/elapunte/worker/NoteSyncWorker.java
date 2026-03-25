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
    private static final int SYNC_TIMEOUT_SECONDS = 30;

    private final NoteRepository repository;
    private final ExecutorService networkExecutor;

    @AssistedInject
    public NoteSyncWorker(
            @Assisted @NonNull Context context,
            @Assisted @NonNull WorkerParameters params,
            NoteRepository repository,
            @Named("gRPCExecutor") ExecutorService networkExecutor) {
        super(context, params);
        this.repository = repository;
        this.networkExecutor = networkExecutor;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(NOTE_LOG_TAG, "NoteSyncWorker: starting pending sync");
        try {
            Future<?> future = networkExecutor.submit(repository::syncPendingNotesSync);
            future.get(SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Log.d(NOTE_LOG_TAG, "NoteSyncWorker: sync completed successfully");
            return Result.success();
        } catch (TimeoutException e) {
            Log.w(NOTE_LOG_TAG, "NoteSyncWorker: sync timed out, will retry");
            return Result.retry();
        } catch (Exception e) {
            Log.e(NOTE_LOG_TAG, "NoteSyncWorker: sync failed", e);
            return Result.retry();
        }
    }
}
