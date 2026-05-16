package com.kimikevin.elapunte.model.repository;

import static com.kimikevin.elapunte.util.AppConstants.NOTE_LOG_TAG;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.kimikevin.elapunte.model.dao.NoteDao;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.proto.CreateNoteRequest;
import com.kimikevin.elapunte.proto.GetAllNotesRequest;
import com.kimikevin.elapunte.proto.NoteIdRequest;
import com.kimikevin.elapunte.proto.NoteListResponse;
import com.kimikevin.elapunte.proto.NoteResponse;
import com.kimikevin.elapunte.proto.NoteServiceGrpc;
import com.kimikevin.elapunte.proto.UpdateNoteRequest;
import com.kimikevin.elapunte.util.NoteMapper;
import com.kimikevin.elapunte.util.TimeAgoUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.hilt.android.qualifiers.ApplicationContext;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class NoteRepository {
    private static final long GRPC_DEADLINE_SECONDS = 10;
    private static final String SYNC_WORK_NAME = "note_pending_sync";
    private static final int PAGE_SIZE = 20;

    private final Context context;
    private final NoteDao noteDao;
    private final NoteServiceGrpc.NoteServiceBlockingStub grpcStub;
    private final ManagedChannel channel;
    private final Object syncLock = new Object();
    private final ExecutorService networkExecutor;

    @Inject
    public NoteRepository(@ApplicationContext Context context,
                          NoteDao noteDao,
                          NoteServiceGrpc.NoteServiceBlockingStub grpcStub,
                          ManagedChannel channel,
                          @Named("gRPCExecutor") ExecutorService networkExecutor) {
        this.context = context;
        this.noteDao = noteDao;
        this.grpcStub = grpcStub;
        this.channel = channel;
        this.networkExecutor = networkExecutor;
    }

    // ── Connectivity ─────────────────────────────────────────────────────────

    private boolean isCurrentlyConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        android.net.Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null &&
                (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    private boolean isBackendReachable() {
        if (!isCurrentlyConnected()) return false;
        ConnectivityState state = channel.getState(true);
        Log.d(NOTE_LOG_TAG, "isBackendReachable: state=" + state);
        return state != ConnectivityState.SHUTDOWN;
    }

    // ── Local read ───────────────────────────────────────────────────────────

    public LiveData<List<Note>> getAllNotes() {
        return noteDao.getAllNotes();
    }

    // ── Full sync (push pending + pull remote) ───────────────────────────────

    public void syncNotes() {
        networkExecutor.execute(() -> {
            if (!isBackendReachable()) {
                Log.d(NOTE_LOG_TAG, "syncNotes: backend not reachable, skipping full sync");
                return;
            }

            try {
                // Step 1 — push all pending local changes first
                syncPendingNotesInternal();

                // Step 2 — build Set of pending IDs once for O(1) lookup
                List<Note> unsyncedNotes = noteDao.getUnsyncedNotes();
                Set<String> pendingIds = new HashSet<>();
                for (Note n : unsyncedNotes) {
                    pendingIds.add(n.getId());
                }

                // Step 3 — pull remote notes page by page
                int page = 0;
                boolean hasMore = true;

                while (hasMore) {
                    GetAllNotesRequest request = GetAllNotesRequest.newBuilder()
                            .setPage(page)
                            .setSize(PAGE_SIZE)
                            .build();

                    NoteListResponse response = grpcStub
                            .withDeadlineAfter(GRPC_DEADLINE_SECONDS, TimeUnit.SECONDS)
                            .getAllNotes(request);

                    List<NoteResponse> notes = response.getNotesList();

                    for (NoteResponse networkNote : notes) {
                        // Skip notes with pending local changes — don't overwrite
                        if (pendingIds.contains(networkNote.getId())) {
                            Log.d(NOTE_LOG_TAG, "syncNotes: skipping pending note id=" + networkNote.getId());
                            continue;
                        }
                        noteDao.insert(NoteMapper.fromProto(networkNote));
                    }

                    Log.d(NOTE_LOG_TAG, "syncNotes: pulled page=" + page + " count=" + notes.size());

                    // If we got a full page there may be more — otherwise we're done
                    hasMore = notes.size() == PAGE_SIZE;
                    page++;
                }

                Log.d(NOTE_LOG_TAG, "syncNotes: all pages pulled successfully totalPages=" + page);

            } catch (StatusRuntimeException e) {
                Log.e(NOTE_LOG_TAG, "syncNotes gRPC error: " + e.getStatus(), e);
            } catch (Exception e) {
                Log.e(NOTE_LOG_TAG, "syncNotes error", e);
            }
        });
    }

    // ── Pending sync ─────────────────────────────────────────────────────────

    public void syncPendingNotes() {
        networkExecutor.execute(this::syncPendingNotesInternal);
    }

    private void syncPendingNotesInternal() {
        synchronized (syncLock) {
            boolean reachable = isBackendReachable();
            Log.d(NOTE_LOG_TAG, "syncPendingNotesInternal: reachable=" + reachable);

            if (!reachable) {
                Log.d(NOTE_LOG_TAG, "syncPendingNotes: backend not reachable, skipping");
                return;
            }

            List<Note> unsyncedNotes = noteDao.getUnsyncedNotes();
            Log.d(NOTE_LOG_TAG, "syncPendingNotes: " + unsyncedNotes.size() + " notes to sync");

            for (Note note : unsyncedNotes) {
                try {
                    String action = note.getPendingAction();
                    if (action == null) continue;

                    Log.d(NOTE_LOG_TAG, "syncPending: pushing note id=" + note.getId() + " action=" + action);

                    switch (action) {
                        case "INSERT":
                            pushInsert(note);
                            break;
                        case "UPDATE":
                            pushUpdate(note);
                            break;
                        case "DELETE":
                            pushDelete(note);
                            break;
                    }

                    if ("DELETE".equals(action)) {
                        noteDao.delete(note);
                    } else {
                        noteDao.markAsSynced(note.getId());
                    }
                    Log.d(NOTE_LOG_TAG, "syncPending: OK note id=" + note.getId() + " action=" + action);

                } catch (Exception e) {
                    Log.e(NOTE_LOG_TAG, "syncPending: FAILED note id=" + note.getId()
                            + ": " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
                }
            }
        }
    }

    public void syncPendingNotesSync() {
        syncPendingNotesInternal();
    }

    // ── WorkManager enqueue ──────────────────────────────────────────────────

    private void enqueueSyncWork() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(
                com.kimikevin.elapunte.worker.NoteSyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                syncRequest
        );
        Log.d(NOTE_LOG_TAG, "enqueueSyncWork: pending sync work enqueued");
    }

    // ── gRPC push helpers ────────────────────────────────────────────────────

    private void pushInsert(Note note) {
        CreateNoteRequest request = NoteMapper.toCreateRequest(note);
        grpcStub.withDeadlineAfter(GRPC_DEADLINE_SECONDS, TimeUnit.SECONDS).createNote(request);
    }

    private void pushUpdate(Note note) {
        UpdateNoteRequest request = UpdateNoteRequest.newBuilder()
                .setId(note.getId())
                .setTitle(note.getTitle() != null ? note.getTitle() : "")
                .setContent(note.getContent() != null ? note.getContent() : "")
                .build();
        grpcStub.withDeadlineAfter(GRPC_DEADLINE_SECONDS, TimeUnit.SECONDS).updateNote(request);
    }

    private void pushDelete(Note note) {
        NoteIdRequest request = NoteIdRequest.newBuilder()
                .setId(note.getId())
                .build();
        grpcStub.withDeadlineAfter(GRPC_DEADLINE_SECONDS, TimeUnit.SECONDS).deleteNote(request);
    }

    // ── Public CRUD (offline-first) ──────────────────────────────────────────

    public void insertNote(Note note) {
        networkExecutor.execute(() -> {
            long timestamp = System.currentTimeMillis();
            note.setTimestamp(timestamp);
            note.setFormattedDate(TimeAgoUtil.getTimeUsing24HourFormat(timestamp));
            note.setPendingAction("INSERT");
            note.setSynced(false);

            noteDao.insert(note);
            Log.d(NOTE_LOG_TAG, "insertNote: saved locally id=" + note.getId());

            if (isBackendReachable()) {
                try {
                    pushInsert(note);
                    noteDao.markAsSynced(note.getId());
                    Log.d(NOTE_LOG_TAG, "insertNote: synced to backend id=" + note.getId());
                } catch (Exception e) {
                    Log.e(NOTE_LOG_TAG, "insertNote: push failed", e);
                    enqueueSyncWork();
                }
            } else {
                Log.d(NOTE_LOG_TAG, "insertNote: offline, pending sync id=" + note.getId());
                enqueueSyncWork();
            }
        });
    }

    public void updateNote(Note note) {
        networkExecutor.execute(() -> {
            note.setPendingAction("UPDATE");
            note.setSynced(false);
            noteDao.insert(note);
            Log.d(NOTE_LOG_TAG, "updateNote: saved locally id=" + note.getId());

            if (isBackendReachable()) {
                try {
                    Log.d(NOTE_LOG_TAG, "updateNote: attempting gRPC push...");
                    pushUpdate(note);
                    noteDao.markAsSynced(note.getId());
                    Log.d(NOTE_LOG_TAG, "updateNote: synced to backend id=" + note.getId());
                } catch (Exception e) {
                    Log.e(NOTE_LOG_TAG, "updateNote: gRPC push failed: " + e.getMessage(), e);
                    enqueueSyncWork();
                }
            } else {
                Log.d(NOTE_LOG_TAG, "updateNote: offline, pending sync id=" + note.getId());
                enqueueSyncWork();
            }
        });
    }

    public void deleteNote(Note note) {
        networkExecutor.execute(() -> {
            Log.d(NOTE_LOG_TAG, "deleteNote: id=" + note.getId()
                    + " isSynced=" + note.isSynced()
                    + " pendingAction=" + note.getPendingAction());

            if (!"INSERT".equals(note.getPendingAction())) {
                // Note exists on backend — soft-delete until backend confirms
                note.setPendingAction("DELETE");
                note.setSynced(false);
                noteDao.insert(note);

                if (isBackendReachable()) {
                    try {
                        pushDelete(note);
                        noteDao.delete(note);
                        Log.d(NOTE_LOG_TAG, "deleteNote: synced to backend id=" + note.getId());
                    } catch (Exception e) {
                        Log.e(NOTE_LOG_TAG, "deleteNote: gRPC push failed: " + e.getMessage(), e);
                        enqueueSyncWork();
                    }
                } else {
                    Log.d(NOTE_LOG_TAG, "deleteNote: offline, marked for deletion id=" + note.getId());
                    enqueueSyncWork();
                }
            } else {
                // Created offline and never synced — delete locally only
                noteDao.delete(note);
                Log.d(NOTE_LOG_TAG, "deleteNote: removed offline-only note id=" + note.getId());
            }
        });
    }
}