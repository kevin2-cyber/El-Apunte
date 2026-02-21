package com.kimikevin.elapunte.model.entity;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


import com.kimikevin.elapunte.BR;

import java.util.Objects;
import java.util.UUID;

@Entity(tableName = "note_table")
public class Note extends BaseObservable {
    @PrimaryKey()
    @ColumnInfo(name = "note_id")
    @NonNull
    private String id;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "content")
    private String content;
    @ColumnInfo(name = "formatted_date")
    private String formattedDate;
    @ColumnInfo(name = "timestamp")
    private long timestamp;

    public Note(String title, String content) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }


    @Ignore
    public Note() {}

    @NonNull
    @Bindable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        notifyPropertyChanged(BR.id);
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        notifyPropertyChanged(BR.content);
    }

    @Bindable
    public String getFormattedDate() {
        return formattedDate;
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
        notifyPropertyChanged(BR.formattedDate);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @NonNull
    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", formattedDate=" + formattedDate +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object obj) {
     if (this == obj) return true;
     if (obj == null || getClass() != obj.getClass()) return false;
     Note note = (Note) obj;
     return Objects.equals(id, note.id)
             && title.equals(note.title)
             && content.equals(note.content)
             && Objects.equals(formattedDate, note.formattedDate)
             && timestamp == note.timestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,title,content,formattedDate,timestamp);
    }
}