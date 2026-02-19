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

@Entity(tableName = "note_table")
public class Note extends BaseObservable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "note_id")
    private int id;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "content")
    private String content;
    @ColumnInfo(name = "formatted_date")
    private String formattedDate;

    public Note(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    @Ignore
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Ignore
    public Note() {}

    @Bindable
    public int getId() {
        return id;
    }

    public void setId(int id) {
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
     return id == note.id
             && title.equals(note.title)
             && content.equals(note.content)
             && Objects.equals(formattedDate, note.formattedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,title,content,formattedDate);
    }
}