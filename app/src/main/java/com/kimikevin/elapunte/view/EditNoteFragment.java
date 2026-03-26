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

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditNoteFragment extends Fragment {
    private FragmentEditNoteBinding binding;
    private NoteViewModel noteViewModel;
    private Note note;
    private String noteId;
    private String originalTitle;
    private String originalContent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            noteId = args.getString(NOTE_ID);
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

        if (noteId != null) {
            note = new Note();
            note.setId(noteId);
            note.setTitle(originalTitle);
            note.setContent(originalContent);
        } else {
            note = new Note(originalTitle, originalContent);
        }

        binding.setNote(note);
        binding.setHandler(new SaveClickHandler());
        binding.setLifecycleOwner(getViewLifecycleOwner());

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public class SaveClickHandler {
        public void onSubmitButtonClicked(View view) {
            String title = note.getTitle() != null ? note.getTitle().trim() : "";
            String content = note.getContent() != null ? note.getContent().trim() : "";

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
                noteViewModel.updateNote(note);
            } else {
                noteViewModel.insertNote(note);
            }

            Navigation.findNavController(view).popBackStack();
        }
    }
}
