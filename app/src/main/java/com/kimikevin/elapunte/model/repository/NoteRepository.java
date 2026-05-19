package com.kimikevin.elapunte.model.repository;

import static com.kimikevin.elapunte.util.AppConstants.NOTE_LOG_TAG;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
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
import com.kimikevin.elapunte.model.network.NoteApi;
import com.kimikevin.elapunte.model.network.NoteDto;
import com.kimikevin.elapunte.util.NoteMapper;
import com.kimikevin.elapunte.util.TimeAgoUtil;
import com.kimikevin.elapunte.worker.NoteSyncWorker;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Response;


public class NoteRepository {
    private static final String SYNC_WORK_NAME = "note_pending_sync";
    private static final int PAGE_SIZE = 20;

    private final Context context;
    private final NoteDao noteDao;
    private final NoteApi noteApi;
    private final Object syncLock = new Object();

    public final ExecutorService networkExecutor;

    @Inject
    public NoteRepository(@ApplicationContext Context context,
                          NoteDao noteDao,
                          NoteApi noteApi,
                          @Named("networkExecutor") ExecutorService networkExecutor) {
        this.context = context;
        this.noteDao = noteDao;
        this.noteApi = noteApi;
        this.networkExecutor = networkExecutor;
    }

    // ── Connectivity ─────────────────────────────────────────────────────────

    private boolean isBackendReachable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        Network network = cm.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(network);
        return caps != null &&
                (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
    }

    // ── Local read ───────────────────────────────────────────────────────────

    public LiveData<List<Note>> getAllNotes() {
        return noteDao.getAllNotes();
    }

    // ── Full sync (push pending + pull remote) ───────────────────────────────

    public void syncNotes() {
        networkExecutor.execute(() -> {
            try {
                syncNotesInternal();
            } catch (Exception e) {
                Log.e(NOTE_LOG_TAG, "syncNotes async error: " + e.getMessage(), e);
            }
        });
    }

    public void syncNotesSync() throws IOException {
        syncNotesInternal();
    }

    private void syncNotesInternal() throws IOException {
        if (!isBackendReachable()) {
            Log.d(NOTE_LOG_TAG, "syncNotes: backend not reachable, skipping full sync");
            return;
        }

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
            Response<List<NoteDto>> response =
                    noteApi.getAllNotes(page, PAGE_SIZE).execute();

            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("syncNotes: HTTP " + response.code());
            }

            List<NoteDto> notes = response.body();

            for (NoteDto networkNote : notes) {
                if (pendingIds.contains(networkNote.getId())) {
                    Log.d(NOTE_LOG_TAG, "syncNotes: skipping pending note id=" + networkNote.getId());
                    continue;
                }
                noteDao.insert(NoteMapper.fromDto(networkNote));
            }

            Log.d(NOTE_LOG_TAG, "syncNotes: pulled page=" + page + " count=" + notes.size());

            hasMore = notes.size() == PAGE_SIZE;
            page++;
        }

        Log.d(NOTE_LOG_TAG, "syncNotes: all pages pulled successfully totalPages=" + page);
    }

    // ── Pending sync ─────────────────────────────────────────────────────────

    public void syncPendingNotes() {
        networkExecutor.execute(() -> {
            try {
                syncPendingNotesInternal();
            } catch (Exception e) {
                Log.e(NOTE_LOG_TAG, "syncPendingNotes async error: " + e.getMessage(), e);
            }
        });
    }

    public void syncPendingNotesSync() {
        syncPendingNotesInternal();
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

    // ── WorkManager enqueue ──────────────────────────────────────────────────

    private void enqueueSyncWork() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(
                NoteSyncWorker.class)
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

    // ── REST push helpers ────────────────────────────────────────────────────

    private void pushInsert(Note note) throws IOException {
        String tempId = note.getId();
        NoteDto payload = NoteMapper.toDto(note);
        payload.setId(null); // server is the source of truth for IDs
        Response<NoteDto> response = noteApi.createNote(payload).execute();
        if (!response.isSuccessful()) {
            throw new IOException("createNote failed: HTTP " + response.code());
        }
        NoteDto body = response.body();
        if (body == null || body.getId() == null) {
            throw new IOException("createNote: response missing id");
        }
        if (!body.getId().equals(tempId)) {
            // Adopt the server-assigned ID locally so subsequent pulls don't duplicate the row.
            noteDao.deleteById(tempId);
            note.setId(body.getId());
            noteDao.insert(note);
            Log.d(NOTE_LOG_TAG, "pushInsert: adopted serverId=" + body.getId() + " (was tempId=" + tempId + ")");
        }
    }

    private void pushUpdate(Note note) throws IOException {
        Response<NoteDto> response = noteApi.updateNote(note.getId(), NoteMapper.toDto(note)).execute();
        if (response.code() == 404) {
            // Server doesn't know this note — fall back to create.
            // Self-heals notes that were edited before their initial INSERT ever synced.
            Log.d(NOTE_LOG_TAG, "pushUpdate: 404 fallback to create id=" + note.getId());
            pushInsert(note);
            return;
        }
        if (!response.isSuccessful()) {
            throw new IOException("updateNote failed: HTTP " + response.code());
        }
    }

    private void pushDelete(Note note) throws IOException {
        Response<Void> response = noteApi.deleteNote(note.getId()).execute();
        if (!response.isSuccessful() && response.code() != 404) {
            throw new IOException("deleteNote failed: HTTP " + response.code());
        }
    }

    // ── Public CRUD (offline-first) ──────────────────────────────────────────

    public void insertNote(Note note) {
        networkExecutor.execute(() -> {
            long timestamp = System.currentTimeMillis();
            note.setTimestamp(timestamp);
            note.setFormattedDate(TimeAgoUtil.formatChatTimestamp(timestamp));
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
            long timestamp = System.currentTimeMillis();
            note.setTimestamp(timestamp);
            note.setFormattedDate(TimeAgoUtil.formatChatTimestamp(timestamp));

            // Preserve INSERT if the note has never been pushed to the server —
            // the eventual POST will carry the latest title/content.
            String existing = noteDao.getPendingActionById(note.getId());
            String action = "INSERT".equals(existing) ? "INSERT" : "UPDATE";
            note.setPendingAction(action);
            note.setSynced(false);
            noteDao.insert(note);
            Log.d(NOTE_LOG_TAG, "updateNote: saved locally id=" + note.getId() + " action=" + action);

            if (isBackendReachable()) {
                try {
                    if ("INSERT".equals(action)) {
                        pushInsert(note);
                    } else {
                        pushUpdate(note);
                    }
                    noteDao.markAsSynced(note.getId());
                    Log.d(NOTE_LOG_TAG, "updateNote: synced to backend id=" + note.getId());
                } catch (Exception e) {
                    Log.e(NOTE_LOG_TAG, "updateNote: push failed: " + e.getMessage(), e);
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
                note.setPendingAction("DELETE");
                note.setSynced(false);
                noteDao.insert(note);

                if (isBackendReachable()) {
                    try {
                        pushDelete(note);
                        noteDao.delete(note);
                        Log.d(NOTE_LOG_TAG, "deleteNote: synced to backend id=" + note.getId());
                    } catch (Exception e) {
                        Log.e(NOTE_LOG_TAG, "deleteNote: push failed: " + e.getMessage(), e);
                        enqueueSyncWork();
                    }
                } else {
                    Log.d(NOTE_LOG_TAG, "deleteNote: offline, marked for deletion id=" + note.getId());
                    enqueueSyncWork();
                }
            } else {
                noteDao.delete(note);
                Log.d(NOTE_LOG_TAG, "deleteNote: removed offline-only note id=" + note.getId());
            }
        });
    }
}