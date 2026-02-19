package com.kimikevin.elapunte.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kimikevin.elapunte.R;
import com.kimikevin.elapunte.databinding.NoteItemBinding;
import com.kimikevin.elapunte.model.entity.Note;

import java.util.Random;

public class NoteAdapter extends ListAdapter<Note, NoteAdapter.NoteViewHolder> {
    private final Context context;
    private OnItemClickListener listener;

    // Step 1: Implement DiffUtil properly
    private static final DiffUtil.ItemCallback<Note> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
                    return oldItem.equals(newItem);
                }

                @Nullable
                @Override
                public Object getChangePayload(@NonNull Note oldItem, @NonNull Note newItem) {
                    return super.getChangePayload(oldItem, newItem);
                }
            };

    public NoteAdapter(Context context) {
        super(DIFF_CALLBACK);
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
        Note note = getItem(position);
        holder.bind(note);
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
                    listener.onNoteClick(getItem(position));
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    showDeleteConfirmationDialog(getItem(position));
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

        private void showDeleteConfirmationDialog(Note note) {
            new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_App_MaterialAlertDialog)
                    .setTitle(R.string.delete_note_title)
                    .setMessage(R.string.delete_note_message)
                    .setPositiveButton(R.string.yes, (dialog, which) -> {
                        if (listener != null) {
                            listener.onNoteDelete(note);
                        }
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