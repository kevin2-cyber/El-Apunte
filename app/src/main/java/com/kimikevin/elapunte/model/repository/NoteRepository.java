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

import io.grpc.StatusRuntimeException;

public class NoteRepository {
    private final NoteDao noteDao;
    private final NoteServiceGrpc.NoteServiceBlockingStub grpcStub;
    private final NetworkMonitor networkMonitor;
    private final Object syncLock = new Object();

    ExecutorService networkExecutor;

    @Inject
    public NoteRepository(NoteDao noteDao,
                          NoteServiceGrpc.NoteServiceBlockingStub grpcStub,
                          @Named("gRPCExecutor") ExecutorService networkExecutor,
                          NetworkMonitor networkMonitor) {
        this.noteDao = noteDao;
        this.grpcStub = grpcStub;
        this.networkExecutor = networkExecutor;
        this.networkMonitor = networkMonitor;
    }

    public LiveData<List<Note>> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public NetworkMonitor getNetworkMonitor() {
        return networkMonitor;
    }

    public void syncNotes() {
        networkExecutor.execute(() -> {
            if (!networkMonitor.isCurrentlyConnected()) {
                Log.d(NOTE_LOG_TAG, "syncNotes: No network, skipping full sync");
                return;
            }

            try {
                // First push all pending local changes to backend
                syncPendingNotesInternal();

                // Then pull remote notes
                Empty request = Empty.newBuilder().build();
                NoteListResponse response = grpcStub.withDeadlineAfter(30, TimeUnit.SECONDS).getAllNotes(request);

                for (NoteResponse networkNote : response.getNotesList()) {
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
            boolean isOnline = networkMonitor.isCurrentlyConnected();
            Log.d(NOTE_LOG_TAG, "syncPendingNotesInternal: isOnline=" + isOnline);

            if (!isOnline) {
                Log.d(NOTE_LOG_TAG, "syncPendingNotes: No network, skipping");
                return;
            }

            List<Note> unsyncedNotes = noteDao.getUnsyncedNotes();
            Log.d(NOTE_LOG_TAG, "syncPendingNotes: " + unsyncedNotes.size() + " notes to sync");

            for (Note note : unsyncedNotes) {
                try {
                    String action = note.getPendingAction();
                    if (action == null) continue;

                    Log.d(NOTE_LOG_TAG, "Syncing note " + note.getId() + " action=" + action);

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
                        noteDao.delete(note); // Remove from local DB after backend confirms
                    } else {
                        noteDao.markAsSynced(note.getId());
                    }
                    Log.d(NOTE_LOG_TAG, "Synced pending note: " + note.getId() + " action: " + action);
                } catch (StatusRuntimeException e) {
                    Log.e(NOTE_LOG_TAG, "Failed to sync pending note " + note.getId() + ": " + e.getMessage(), e);
                    // Leave as unsynced — will retry on next connectivity restore
                } catch (Exception e) {
                    Log.e(NOTE_LOG_TAG, "syncPendingNotes error for " + note.getId(), e);
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
        grpcStub.withDeadlineAfter(30, TimeUnit.SECONDS).createNote(request);
    }

    private void pushUpdate(Note note) {
        NoteResponse request = NoteResponse.newBuilder()
                .setId(note.getId())
                .setTitle(note.getTitle() != null ? note.getTitle() : "")
                .setContent(note.getContent() != null ? note.getContent() : "")
                .setFormattedDate(note.getFormattedDate() != null ? note.getFormattedDate() : "")
                .build();
        grpcStub.withDeadlineAfter(30, TimeUnit.SECONDS).updateNote(request);
    }

    private void pushDelete(Note note) {
        NoteIdRequest request = NoteIdRequest.newBuilder()
                .setId(note.getId())
                .build();
        grpcStub.withDeadlineAfter(30, TimeUnit.SECONDS).deleteNote(request);
    }

    // ── Public CRUD (offline-first) ──────────────────────────────────────

    public void insertNote(Note note) {
        networkExecutor.execute(() -> {
            // Always save locally first
            note.setPendingAction("INSERT");
            note.setSynced(false);
            noteDao.insert(note);

            // Try to sync immediately if online
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    pushInsert(note);
                    noteDao.markAsSynced(note.getId());
                    Log.d(NOTE_LOG_TAG, "Note synced to backend: " + note.getId());
                } catch (StatusRuntimeException e) {
                    Log.e(NOTE_LOG_TAG, "Failed to sync note, will retry later: " + e.getMessage(), e);
                } catch (Exception e) {
                    Log.e(NOTE_LOG_TAG, "insertNote sync error", e);
                }
            } else {
                Log.d(NOTE_LOG_TAG, "Offline: note saved locally, pending sync: " + note.getId());
            }
        });
    }

    public void updateNote(Note note) {
        networkExecutor.execute(() -> {
            // Always update locally first
            note.setPendingAction("UPDATE");
            note.setSynced(false);
            noteDao.update(note);

            // Try to sync immediately if online
            if (networkMonitor.isCurrentlyConnected()) {
                try {
                    pushUpdate(note);
                    noteDao.markAsSynced(note.getId());
                    Log.d(NOTE_LOG_TAG, "Note updated on backend: " + note.getId());
                } catch (StatusRuntimeException e) {
                    Log.e(NOTE_LOG_TAG, "Failed to update note on backend: " + e.getMessage(), e);
                } catch (Exception e) {
                    Log.e(NOTE_LOG_TAG, "updateNote sync error", e);
                }
            } else {
                Log.d(NOTE_LOG_TAG, "Offline: note updated locally, pending sync: " + note.getId());
            }
        });
    }

    public void deleteNote(Note note) {
        networkExecutor.execute(() -> {
            if (note.isSynced() || (note.getPendingAction() != null && !"INSERT".equals(note.getPendingAction()))) {
                // Note exists on backend — soft-delete until backend confirms
                note.setPendingAction("DELETE");
                note.setSynced(false);
                noteDao.update(note);

                if (networkMonitor.isCurrentlyConnected()) {
                    try {
                        pushDelete(note);
                        noteDao.delete(note); // Remove locally after backend confirms
                        Log.d(NOTE_LOG_TAG, "Note deleted on backend: " + note.getId());
                    } catch (StatusRuntimeException e) {
                        Log.e(NOTE_LOG_TAG, "Failed to delete note on backend: " + e.getMessage(), e);
                        // Stays marked as DELETE pending
                    } catch (Exception e) {
                        Log.e(NOTE_LOG_TAG, "deleteNote sync error", e);
                    }
                } else {
                    Log.d(NOTE_LOG_TAG, "Offline: note marked for deletion, pending sync: " + note.getId());
                }
            } else {
                // Note was created offline and never synced — just delete locally
                noteDao.delete(note);
                Log.d(NOTE_LOG_TAG, "Deleted offline-only note: " + note.getId());
            }
        });
    }
}

