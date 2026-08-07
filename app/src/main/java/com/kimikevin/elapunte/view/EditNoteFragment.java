package com.kimikevin.elapunte.view;

import static com.kimikevin.elapunte.util.AppConstants.NOTE_CONTENT;
import static com.kimikevin.elapunte.util.AppConstants.NOTE_ID;
import static com.kimikevin.elapunte.util.AppConstants.NOTE_TITLE;

import android.os.Bundle;
import android.text.TextUtils;
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
        Bundle args = getArguments();
        if (args != null && args.containsKey(NOTE_ID)) {
            String idStr = args.getString(NOTE_ID);
            if (idStr != null) {
                noteId = UUID.fromString(idStr);
            }
            originalTitle = args.getString(NOTE_TITLE, "");
            originalContent = args.getString(NOTE_CONTENT, "");
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

        if (noteViewModel.getCurrentNote().getValue() == null) {
            if (noteId != null) {
                Note note = new Note(originalTitle, originalContent);
                note.setId(noteId);
                noteViewModel.setCurrentNote(note);
            } else {
                noteViewModel.setCurrentNote(new Note());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public class SaveClickHandler {
        public void onSubmitButtonClicked(View view) {
            Note currentNote = noteViewModel.getCurrentNote().getValue();
            if (currentNote == null) return;

            String title = currentNote.getTitle() != null ? currentNote.getTitle().trim() : "";
            String content = currentNote.getContent() != null ? currentNote.getContent().trim() : "";

            if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
                Toast.makeText(requireContext(), R.string.empty_note_error, Toast.LENGTH_LONG).show();
                return;
            }

            boolean hasChanges = !title.equals(originalTitle) || !content.equals(originalContent);

            if (!hasChanges) {
                Toast.makeText(requireContext(), R.string.no_changes_detected, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).popBackStack();
                return;
            }

            if (noteId != null) {
                noteViewModel.updateNote(currentNote);
            } else {
                noteViewModel.insertNote(currentNote);
            }

            Navigation.findNavController(view).popBackStack();
        }
    }
}
