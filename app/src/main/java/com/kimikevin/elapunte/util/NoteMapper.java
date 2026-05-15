package com.kimikevin.elapunte.util;

import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.proto.CreateNoteRequest;
import com.kimikevin.elapunte.proto.NoteResponse;

public final class NoteMapper {
    private NoteMapper() {}

    /** Maps a proto NoteResponse to a local Room Note entity. */
    public static Note fromProto(NoteResponse proto) {
        Note note = new Note();
        note.setId(proto.getId());
        note.setTitle(proto.getTitle());
        note.setContent(proto.getContent());
        note.setFormattedDate(proto.getFormattedDate());
        note.setTimestamp(System.currentTimeMillis());
        note.setSynced(true);
        note.setPendingAction(null);
        return note;
    }

    /** Maps a local Room Note entity to a proto CreateNoteRequest. */
    public static CreateNoteRequest toCreateRequest(Note note) {
        return CreateNoteRequest.newBuilder()
                .setId(note.getId())
                .setTitle(note.getTitle() != null ? note.getTitle() : "")
                .setContent(note.getContent() != null ? note.getContent() : "")
                .setFormattedDate(TimeAgoUtil.getBackendDate(note.getTimestamp()))
                .build();
    }
}
