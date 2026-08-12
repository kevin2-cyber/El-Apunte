package com.kimikevin.elapunte.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.kimikevin.elapunte.R;
import com.kimikevin.elapunte.databinding.FragmentEditNoteBinding;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.viewmodel.NoteViewModel;

import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditNoteFragment extends Fragment {
    private FragmentEditNoteBinding binding;
    private NoteViewModel noteViewModel;
    private UUID noteId;
    private String originalTitle;
    private String originalContent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            EditNoteFragmentArgs args = EditNoteFragmentArgs.fromBundle(getArguments());
            String idStr = args.getNoteId();
            if (idStr != null) {
                noteId = UUID.fromString(idStr);
            }
            originalTitle = args.getNoteTitle() != null ? args.getNoteTitle() : "";
            originalContent = args.getNoteContent() != null ? args.getNoteContent() : "";
        } else {
            originalTitle = "";
            originalContent = "";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_note, container, false);
        binding.setHandler(new SaveClickHandler());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        binding.setVm(noteViewModel);

        binding.setLifecycleOwner(getViewLifecycleOwner());

        if (savedInstanceState == null) {
            if (noteId != null) {
                Note note = new Note(originalTitle, originalContent);
                note.setId(noteId);
                noteViewModel.setCurrentNote(note);
            } else {
                noteViewModel.setCurrentNote(new Note());
            }
        }

        noteViewModel.getSaveStatus().observe(getViewLifecycleOwner(), status -> {
            if (status == null) return;

            switch (status) {
                case EMPTY -> Toast.makeText(requireContext(), R.string.empty_note_error, Toast.LENGTH_LONG).show();
                case NO_CHANGES -> Toast.makeText(requireContext(), R.string.no_changes_detected, Toast.LENGTH_LONG).show();
                case SUCCESS -> Navigation.findNavController(requireView()).popBackStack();
            }

            noteViewModel.resetSaveStatus();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public class SaveClickHandler {
        public void onSubmitButtonClicked(View view) {
         noteViewModel.saveNote(originalTitle, originalContent, noteId != null);
        }
    }
}
