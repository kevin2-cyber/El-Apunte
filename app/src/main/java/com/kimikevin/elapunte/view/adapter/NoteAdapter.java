package com.kimikevin.elapunte.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.kimikevin.elapunte.R;
import com.kimikevin.elapunte.databinding.NoteItemBinding;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.view.util.NoteDiffCallback;

import java.util.ArrayList;
import java.util.Random;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private OnItemClickListener listener;
    private ArrayList<Note> notes = new ArrayList<>();
   private Context context;

    public NoteAdapter(Context context) {
        this.context = context;
    }

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
        int[] androidColors = context.getResources().getIntArray(R.array.note_accent_colors);
        int randomAndroidColor = androidColors[new Random().nextInt(androidColors.length)];
        holder.binding.card.setCardBackgroundColor(randomAndroidColor);
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

    public void setFilter(ArrayList<Note> filteredList) {
        this.notes = filteredList;
        final DiffUtil.DiffResult result =
                DiffUtil.calculateDiff(new NoteDiffCallback(notes, filteredList), false);

        notes = filteredList;
        result.dispatchUpdatesTo(NoteAdapter.this);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        private NoteItemBinding binding;

        public NoteViewHolder(@NonNull NoteItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

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
