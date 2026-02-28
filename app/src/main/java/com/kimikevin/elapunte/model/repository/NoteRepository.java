package com.kimikevin.elapunte.model.repository;


import static com.kimikevin.elapunte.util.AppConstants.NOTE_LOG_TAG;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.kimikevin.elapunte.model.dao.NoteDao;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.proto.CreateNoteRequest;
import com.kimikevin.elapunte.proto.Empty;
import com.kimikevin.elapunte.proto.NoteIdRequest;
import com.kimikevin.elapunte.proto.NoteListResponse;
import com.kimikevin.elapunte.proto.NoteResponse;
import com.kimikevin.elapunte.proto.NoteServiceGrpc;
import com.kimikevin.elapunte.util.NetworkMonitor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;

public class NoteRepository {
    private static final long GRPC_DEADLINE_SECONDS = 10;

    private final NoteDao noteDao;
    private final NoteServiceGrpc.NoteServiceBlockingStub grpcStub;
    private final ManagedChannel channel;
    private final NetworkMonitor networkMonitor;
    private final Object syncLock = new Object();

    ExecutorService networkExecutor;

    @Inject
    public NoteRepository(NoteDao noteDao,
                          NoteServiceGrpc.NoteServiceBlockingStub grpcStub,
                          ManagedChannel channel,
                          @Named("gRPCExecutor") ExecutorService networkExecutor,
                          NetworkMonitor networkMonitor) {
        this.noteDao = noteDao;
        this.grpcStub = grpcStub;
        this.channel = channel;
        this.networkExecutor = networkExecutor;
        this.networkMonitor = networkMonitor;
    }

    /**
     * Quick check: device has network and gRPC channel is not shut down.
     * Kicks the channel to start connecting if idle.
     */
    private boolean isBackendReachable() {
        if (!networkMonitor.isCurrentlyConnected()) {
            return false;
        }
        ConnectivityState state = channel.getState(true); // true = request connect if IDLE
        Log.d(NOTE_LOG_TAG, "isBackendReachable: state=" + state);
        return state != ConnectivityState.SHUTDOWN;
    }

    public LiveData<List<Note>> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public NetworkMonitor getNetworkMonitor() {
        return networkMonitor;
    }

    public void syncNotes() {
        networkExecutor.execute(() -> {
            if (!isBackendReachable()) {
                Log.d(NOTE_LOG_TAG, "syncNotes: backend not reachable, skipping full sync");
                return;
            }

            try {
                // First push all pending local changes to backend
                syncPendingNotesInternal();

                // Then pull remote notes
                Empty request = Empty.newBuilder().build();
                NoteListResponse response = grpcStub.withDeadlineAfter(GRPC_DEADLINE_SECONDS, TimeUnit.SECONDS).getAllNotes(request);

                for (NoteResponse networkNote : response.getNotesList()) {
                    // Check if this note has pending local changes — don't overwrite
                    List<Note> pending = noteDao.getUnsyncedNotes();
                    boolean hasPendingChange = false;
                    for (Note p : pending) {
                        if (p.getId().equals(networkNote.getId())) {
                            hasPendingChange = true;
                            break;
                        }
                    }
                    if (hasPendingChange) {
                        Log.d(NOTE_LOG_TAG, "syncNotes: skipping pull for note with pending local changes: " + networkNote.getId());
                        continue;
                    }

                    Note localNote = new Note();
                    localNote.setId(networkNote.getId());
                    localNote.setTitle(networkNote.getTitle());
                    localNote.setContent(networkNote.getContent());
                    localNote.setFormattedDate(networkNote.getFormattedDate());
                    localNote.setTimestamp(System.currentTimeMillis());
                    localNote.setSynced(true);
                    localNote.setPendingAction(null);

                    noteDao.insert(localNote);
                }
                Log.d(NOTE_LOG_TAG, "syncNotes: pulled " + response.getNotesList().size() + " notes from backend");
            } catch (StatusRuntimeException e) {
                Log.e(NOTE_LOG_TAG, "syncNotes: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e(NOTE_LOG_TAG, "syncNotes error", e);
            }
        });
    }

    /**
     * Called when connectivity is restored to push all pending offline changes.
     */
    public void syncPendingNotes() {
        networkExecutor.execute(this::syncPendingNotesInternal);
    }

    /**
     * Internal method that pushes all unsynced local notes to the backend.
     * Must be called from a background thread.
     */
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

                    Log.d(NOTE_LOG_TAG, "syncPending: pushing note " + note.getId() + " action=" + action
                            + " title=" + note.getTitle());

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

                    // Mark as synced after successful backend call
                    if ("DELETE".equals(action)) {
                        noteDao.delete(note);
                    } else {
                        noteDao.markAsSynced(note.getId());
                    }
                    Log.d(NOTE_LOG_TAG, "syncPending: OK note " + note.getId() + " action=" + action);
                } catch (Exception e) {
                    Log.e(NOTE_LOG_TAG, "syncPending: FAILED note " + note.getId()
                            + ": " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
                }
            }
        }
    }

    // ── gRPC push helpers ────────────────────────────────────────────────

    private void pushInsert(Note note) {
        CreateNoteRequest request = CreateNoteRequest.newBuilder()
                .setId(note.getId())
                .setTitle(note.getTitle() != null ? note.getTitle() : "")
                .setContent(note.getContent() != null ? note.getContent() : "")
                .setFormattedDate(note.getFormattedDate() != null ? note.getFormattedDate() : "")
                .build();
        grpcStub.withDeadlineAfter(GRPC_DEADLINE_SECONDS, TimeUnit.SECONDS).createNote(request);
    }

    private void pushUpdate(Note note) {
        NoteResponse request = NoteResponse.newBuilder()
                .setId(note.getId())
                .setTitle(note.getTitle() != null ? note.getTitle() : "")
                .setContent(note.getContent() != null ? note.getContent() : "")
                .setFormattedDate(note.getFormattedDate() != null ? note.getFormattedDate() : "")
                .build();
        grpcStub.withDeadlineAfter(GRPC_DEADLINE_SECONDS, TimeUnit.SECONDS).updateNote(request);
    }

    private void pushDelete(Note note) {
        NoteIdRequest request = NoteIdRequest.newBuilder()
                .setId(note.getId())
                .build();
        grpcStub.withDeadlineAfter(GRPC_DEADLINE_SECONDS, TimeUnit.SECONDS).deleteNote(request);
    }

    // ── Public CRUD (offline-first) ──────────────────────────────────────

    public void insertNote(Note note) {
        networkExecutor.execute(() -> {
            // Always save locally first
            note.setPendingAction("INSERT");
            note.setSynced(false);
            noteDao.insert(note);
            Log.d(NOTE_LOG_TAG, "insertNote: saved locally id=" + note.getId());

            // Try to sync immediately if backend reachable
            if (isBackendReachable()) {
                try {
                    Log.d(NOTE_LOG_TAG, "insertNote: attempting gRPC push...");
                    pushInsert(note);
                    noteDao.markAsSynced(note.getId());
                    Log.d(NOTE_LOG_TAG, "insertNote: synced to backend OK id=" + note.getId());
                } catch (Exception e) {
                    Log.e(NOTE_LOG_TAG, "insertNote: gRPC push failed, will retry later: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
                }
            } else {
                Log.d(NOTE_LOG_TAG, "insertNote: offline, pending sync id=" + note.getId());
            }
        });
    }

    public void updateNote(Note note) {
        networkExecutor.execute(() -> {
            // Always update locally first — use insert with REPLACE so it works
            // even if Room's @Update finds no matching row
            note.setPendingAction("UPDATE");
            note.setSynced(false);
            noteDao.insert(note); // REPLACE strategy ensures upsert by primary key
            Log.d(NOTE_LOG_TAG, "updateNote: saved locally id=" + note.getId());

            // Try to sync immediately if backend reachable
            if (isBackendReachable()) {
                try {
                    Log.d(NOTE_LOG_TAG, "updateNote: attempting gRPC push...");
                    pushUpdate(note);
                    noteDao.markAsSynced(note.getId());
                    Log.d(NOTE_LOG_TAG, "updateNote: synced to backend OK id=" + note.getId());
                } catch (Exception e) {
                    Log.e(NOTE_LOG_TAG, "updateNote: gRPC push failed: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
                }
            } else {
                Log.d(NOTE_LOG_TAG, "updateNote: offline, pending sync id=" + note.getId());
            }
        });
    }

    public void deleteNote(Note note) {
        networkExecutor.execute(() -> {
            Log.d(NOTE_LOG_TAG, "deleteNote: id=" + note.getId() + " isSynced=" + note.isSynced() + " pendingAction=" + note.getPendingAction());

            if (note.isSynced() || (note.getPendingAction() != null && !"INSERT".equals(note.getPendingAction()))) {
                // Note exists on backend — soft-delete until backend confirms
                note.setPendingAction("DELETE");
                note.setSynced(false);
                noteDao.insert(note); // REPLACE to ensure it persists

                if (isBackendReachable()) {
                    try {
                        pushDelete(note);
                        noteDao.delete(note); // Remove locally after backend confirms
                        Log.d(NOTE_LOG_TAG, "deleteNote: synced to backend OK id=" + note.getId());
                    } catch (Exception e) {
                        Log.e(NOTE_LOG_TAG, "deleteNote: gRPC push failed: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
                        // Stays marked as DELETE pending
                    }
                } else {
                    Log.d(NOTE_LOG_TAG, "deleteNote: offline, marked for deletion id=" + note.getId());
                }
            } else {
                // Note was created offline and never synced — just delete locally
                noteDao.delete(note);
                Log.d(NOTE_LOG_TAG, "deleteNote: removed offline-only note id=" + note.getId());
            }
        });
    }
}

