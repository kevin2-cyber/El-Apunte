package com.kimikevin.elapunte.util;

import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.model.network.NoteDto;

public final class NoteMapper {
    private NoteMapper() {}

    public static Note fromDto(NoteDto dto) {
        Note note = new Note();
        note.setId(dto.getId());
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setFormattedDate(dto.getFormattedDate());
        note.setTimestamp(dto.getTimestamp() > 0 ? dto.getTimestamp() : System.currentTimeMillis());
        note.setSynced(true);
        note.setPendingAction(null);
        return note;
    }

    public static NoteDto toDto(Note note) {
        return new NoteDto(
                note.getId(),
                note.getTitle() != null ? note.getTitle() : "",
                note.getContent() != null ? note.getContent() : "",
                TimeAgoUtil.getBackendDate(note.getTimestamp()),
                note.getTimestamp()
        );
    }
}
