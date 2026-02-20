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

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import io.grpc.StatusRuntimeException;

public class NoteRepository {
    private final NoteDao noteDao;
    private final NoteServiceGrpc.NoteServiceBlockingStub grpcStub;

    ExecutorService networkExecutor;

    @Inject
    public NoteRepository(NoteDao noteDao,
                          NoteServiceGrpc.NoteServiceBlockingStub grpcStub,
                          @Named("gRPCExecutor") ExecutorService networkExecutor) {
        this.noteDao = noteDao;
        this.grpcStub = grpcStub;
        this.networkExecutor = networkExecutor;
    }

    public LiveData<List<Note>> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public void syncNotes() {
        networkExecutor.execute(() -> {
            try {
                Empty request = Empty.newBuilder().build();
                NoteListResponse response = grpcStub.withDeadlineAfter(30, TimeUnit.SECONDS).getAllNotes(request);

                for (NoteResponse networkNote: response.getNotesList()) {
                    Note localNote = new Note();

                    localNote.setId(networkNote.getId());
                    localNote.setTitle(networkNote.getTitle());
                    localNote.setContent(networkNote.getContent());
                    localNote.setFormattedDate(networkNote.getFormattedDate());

                    noteDao.insert(localNote);
                }
            } catch (StatusRuntimeException e) {
                Log.e(NOTE_LOG_TAG, "syncNotes: " + e.getMessage(), e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void insertNote(Note note) {
        networkExecutor.execute(() -> {
            // Save locally first (UUID already generated in Note constructor)
            noteDao.insert(note);

            // Sync to gRPC backend
            try {
                CreateNoteRequest request = CreateNoteRequest.newBuilder()
                        .setId(note.getId())
                        .setTitle(note.getTitle() != null ? note.getTitle() : "")
                        .setContent(note.getContent() != null ? note.getContent() : "")
                        .setFormattedDate(note.getFormattedDate() != null ? note.getFormattedDate() : "")
                        .build();

                grpcStub.withDeadlineAfter(30, TimeUnit.SECONDS).createNote(request);
                Log.d(NOTE_LOG_TAG, "Note synced to backend: " + note.getId());
            } catch (StatusRuntimeException e) {
                Log.e(NOTE_LOG_TAG, "Failed to sync note to backend: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e(NOTE_LOG_TAG, "insertNote sync error", e);
            }
        });
    }

    public void updateNote(Note note) {
        networkExecutor.execute(() -> {
            // Update locally
            noteDao.update(note);

            // Sync update to gRPC backend
            try {
                NoteResponse request = NoteResponse.newBuilder()
                        .setId(note.getId())
                        .setTitle(note.getTitle() != null ? note.getTitle() : "")
                        .setContent(note.getContent() != null ? note.getContent() : "")
                        .setFormattedDate(note.getFormattedDate() != null ? note.getFormattedDate() : "")
                        .build();

                grpcStub.withDeadlineAfter(30, TimeUnit.SECONDS).updateNote(request);
                Log.d(NOTE_LOG_TAG, "Note updated on backend: " + note.getId());
            } catch (StatusRuntimeException e) {
                Log.e(NOTE_LOG_TAG, "Failed to update note on backend: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e(NOTE_LOG_TAG, "updateNote sync error", e);
            }
        });
    }

    public void deleteNote(Note note) {
        networkExecutor.execute(() -> {
            // Delete locally
            noteDao.delete(note);

            // Sync deletion to gRPC backend
            try {
                NoteIdRequest request = NoteIdRequest.newBuilder()
                        .setId(note.getId())
                        .build();

                grpcStub.withDeadlineAfter(30, TimeUnit.SECONDS).deleteNote(request);
                Log.d(NOTE_LOG_TAG, "Note deleted on backend: " + note.getId());
            } catch (StatusRuntimeException e) {
                Log.e(NOTE_LOG_TAG, "Failed to delete note on backend: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e(NOTE_LOG_TAG, "deleteNote sync error", e);
            }
        });
    }
}