package com.kimikevin.el_apunte.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kimikevin.el_apunte.R;
import com.kimikevin.el_apunte.databinding.NoteItemBinding;
import com.kimikevin.el_apunte.model.entity.Note;
import com.kimikevin.el_apunte.view.util.NoteDiffCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private final Context context;
    private List<Note> notes = new ArrayList<>();
    private OnItemClickListener listener;

    public NoteAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NoteItemBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.note_item,
                parent,
                false
        );
        return new NoteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNotes(List<Note> newNotes) {
        DiffUtil.DiffResult result = DiffUtil
                .calculateDiff(new NoteDiffCallback((ArrayList<Note>) notes, (ArrayList<Note>) newNotes));
        notes = newNotes;
        result.dispatchUpdatesTo(this);
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        private final NoteItemBinding binding;

        NoteViewHolder(NoteItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNoteClick(notes.get(position));
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    showDeleteConfirmationDialog(notes.get(position), position);
                }
                return true;
            });
        }

        void bind(Note note) {
            binding.setNote(note);

            // Set random card color
            int[] colors = context.getResources().getIntArray(R.array.note_accent_colors);
            int randomColor = colors[new Random().nextInt(colors.length)];
            binding.card.setCardBackgroundColor(randomColor);

            // Add animation
            binding.card.startAnimation(
                    AnimationUtils.loadAnimation(context, R.anim.anim_four)
            );
        }

        private void showDeleteConfirmationDialog(Note note, int position) {
            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_App_MaterialAlertDialog)
                    .setTitle(R.string.delete_note_title)
                    .setMessage(R.string.delete_note_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        listener.onNoteDelete(note);
                        notes.remove(position);
                        notifyItemRemoved(position);
                    })
                    .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                    .create()
                    .show();
        }
    }

    public interface OnItemClickListener {
        void onNoteClick(Note note);
        void onNoteDelete(Note note);
    }
}