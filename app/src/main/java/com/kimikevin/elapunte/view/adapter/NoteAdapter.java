package com.kimikevin.elapunte.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.kimikevin.elapunte.MainActivity;
import com.kimikevin.elapunte.R;
import com.kimikevin.elapunte.databinding.NoteItemBinding;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.view.util.NoteDiffCallback;
import com.kimikevin.elapunte.view.util.NoteUtil;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private OnItemClickListener listener;
    private ArrayList<Note> notes = new ArrayList<>();

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NoteItemBinding itemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),R.layout.note_item, parent, false);
        return new NoteViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.binding.setNote(currentNote);
        holder.binding.card.setCardBackgroundColor(NoteUtil.getColor());
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    public void setNotes(ArrayList<Note> newNotes) {
        final DiffUtil.DiffResult result =
                DiffUtil.calculateDiff(new NoteDiffCallback(notes, newNotes), false);

        notes = newNotes;
        result.dispatchUpdatesTo(NoteAdapter.this);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        private NoteItemBinding binding;

        public NoteViewHolder(@NonNull NoteItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

//            binding.getRoot().setBackgroundColor(NoteUtil.getColor());
            binding.getRoot().setOnClickListener(v -> {
                int clickedPosition = getAdapterPosition();
                if (listener != null && clickedPosition != RecyclerView.NO_POSITION) {
                    listener.onItemClick(notes.get(clickedPosition));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
