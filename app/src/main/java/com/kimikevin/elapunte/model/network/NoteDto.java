package com.kimikevin.elapunte.model.network;

import java.util.UUID;

public class NoteDto {
    private UUID id;
    private String title;
    private String content;
    private String formattedDate;
    private long timestamp;

    public NoteDto() {}

    public NoteDto(UUID id, String title, String content, String formattedDate, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.formattedDate = formattedDate;
        this.timestamp = timestamp;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getFormattedDate() { return formattedDate; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
