package com.kimikevin.elapunte.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.kimikevin.elapunte.R;
import com.kimikevin.elapunte.databinding.NoteItemBinding;
import com.kimikevin.elapunte.model.entity.Note;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {

    private ArrayList<Note> notes;

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NoteItemBinding itemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),R.layout.note_item, parent, false);

        return new RecyclerViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.binding.setNote(currentNote);
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    public void setNotes(ArrayList<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private NoteItemBinding binding;

        public RecyclerViewHolder(@NonNull NoteItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
