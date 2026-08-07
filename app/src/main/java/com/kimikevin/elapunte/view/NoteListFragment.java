package com.kimikevin.elapunte.view;

import static com.kimikevin.elapunte.util.AppConstants.NOTE_CONTENT;
import static com.kimikevin.elapunte.util.AppConstants.NOTE_ID;
import static com.kimikevin.elapunte.util.AppConstants.NOTE_TITLE;
import static com.kimikevin.elapunte.util.AppConstants.TAG;

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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.kimikevin.elapunte.R;
import com.kimikevin.elapunte.databinding.FragmentNoteListBinding;
import com.kimikevin.elapunte.model.entity.Note;
import com.kimikevin.elapunte.view.adapter.NoteAdapter;
import com.kimikevin.elapunte.viewmodel.NoteViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NoteListFragment extends Fragment {
    private FragmentNoteListBinding binding;
    private NoteViewModel noteViewModel;
    private NoteAdapter noteAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_note_list, container, false);
        binding.setClickHandler(new NoteClickHandler());
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        setupViewModel();
    }

    private void setupViewModel() {
        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);
        noteViewModel.getFilteredNotes().observe(getViewLifecycleOwner(), notes -> {
            noteAdapter.submitList(new ArrayList<>(notes));
            binding.emptyState.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        });

        noteViewModel.getIsReverseLayout().observe(getViewLifecycleOwner(), isReverse -> {
            LinearLayoutManager manager = (LinearLayoutManager) binding.rvNotes.getLayoutManager();
            if (manager != null) {
                manager.setReverseLayout(isReverse);
                manager.setStackFromEnd(isReverse);
            }
        });
    }

    private void setupUI() {
        binding.themeSwitch.setOnClickListener(view -> {
            ThemeBottomSheet themeBottomSheet = new ThemeBottomSheet();
            themeBottomSheet.show(getParentFragmentManager(), TAG);
        });

        setupSearchView();
        initRecyclerView();
    }

    private void setupSearchView() {
        binding.searchView.clearFocus();
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                noteViewModel.setSearchQuery(newText);
                return true;
            }
        });
    }

    private void initRecyclerView() {
        noteAdapter = new NoteAdapter(requireContext());
        noteAdapter.setListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onNoteClick(Note note) {
                Bundle args = new Bundle();
                args.putString(NOTE_ID, note.getId().toString());
                args.putString(NOTE_TITLE, note.getTitle());
                args.putString(NOTE_CONTENT, note.getContent());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_noteListFragment_to_editNoteFragment, args);
            }

            @Override
            public void onNoteDelete(Note note) {
                noteViewModel.deleteNote(note);
                Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.rvNotes.setLayoutManager(layoutManager);
        binding.rvNotes.setItemAnimator(new DefaultItemAnimator());
        binding.rvNotes.setAdapter(noteAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public class NoteClickHandler {
        public void onFabClick(View view) {
            Navigation.findNavController(view)
                    .navigate(R.id.action_noteListFragment_to_editNoteFragment);
        }

        public void onFilterClick(View view) {
           noteViewModel.toggleLayoutOrder();
        }
    }
}
