package com.kimikevin.el_apunte.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.kimikevin.el_apunte.R;
import com.kimikevin.el_apunte.databinding.NoteItemBinding;
import com.kimikevin.el_apunte.model.entity.Note;
import com.kimikevin.el_apunte.view.util.NoteDiffCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private OnItemClickListener listener;
    private ArrayList<Note> notes = new ArrayList<>();
    private final Context context;

    public NoteAdapter(Context context) {
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFilterList(List<Note> filterList) {
        notes = (ArrayList<Note>) filterList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NoteItemBinding itemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.note_item, parent, false);
        return new NoteViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note currentNote = notes.get(position);
        holder.binding.setNote(currentNote);
        int[] androidColors = context.getResources().getIntArray(R.array.note_accent_colors);
        int randomAndroidColor = androidColors[new Random().nextInt(androidColors.length)];
        holder.binding.card.setCardBackgroundColor(randomAndroidColor);
        holder.binding.card.startAnimation(
                AnimationUtils.loadAnimation(holder.binding.card.getContext(), R.anim.anim_four)
        );
        holder.binding.getRoot().setOnLongClickListener(view -> {
            showDeleteConfirmationDialog(holder.binding.getRoot().getContext(), currentNote, position);
            return true;  // Return true to indicate the event is handled
        });
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

    private void showDeleteConfirmationDialog(Context context, Note note, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    listener.onNoteDelete(note); // Perform deletion
                    notes.remove(position); // Remove from list
                    notifyItemRemoved(position); // Update the RecyclerView
                    Log.d("NOTE_DEBUG", "Time Deleted: " + note.getFormattedDate());
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        private final NoteItemBinding binding;

        public NoteViewHolder(@NonNull NoteItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int clickedPosition = getAdapterPosition();
                if (listener != null && clickedPosition != RecyclerView.NO_POSITION) {
                    listener.onNoteClick(notes.get(clickedPosition));
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onNoteClick(Note note);
        void onNoteDelete(Note note);
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}