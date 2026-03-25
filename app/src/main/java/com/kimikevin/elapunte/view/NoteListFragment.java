package com.kimikevin.elapunte.view;

import static com.kimikevin.elapunte.util.AppConstants.NOTE_CONTENT;
import static com.kimikevin.elapunte.util.AppConstants.NOTE_ID;
import static com.kimikevin.elapunte.util.AppConstants.NOTE_LOG_TAG;
import static com.kimikevin.elapunte.util.AppConstants.NOTE_TITLE;
import static com.kimikevin.elapunte.util.AppConstants.TAG;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NoteListFragment extends Fragment {
    private FragmentNoteListBinding binding;
    private NoteViewModel noteViewModel;
    private final ArrayList<Note> noteList = new ArrayList<>();
    private NoteAdapter noteAdapter;
    private String currentQuery = "";
    private boolean isReverseLayout = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentQuery = savedInstanceState.getString("CURRENT_QUERY", "");
            isReverseLayout = savedInstanceState.getBoolean("IS_REVERSE_LAYOUT", false);
        }
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
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            noteList.clear();
            noteList.addAll(notes);

            if (TextUtils.isEmpty(currentQuery)) {
                noteAdapter.submitList(new ArrayList<>(notes));
            } else {
                filterNotes(currentQuery);
            }

            binding.emptyState.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
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
                currentQuery = newText;
                filterNotes(newText);
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
                args.putString(NOTE_ID, note.getId());
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
        layoutManager.setReverseLayout(isReverseLayout);
        layoutManager.setStackFromEnd(isReverseLayout);
        binding.rvNotes.setLayoutManager(layoutManager);
        binding.rvNotes.setItemAnimator(new DefaultItemAnimator());
        binding.rvNotes.setAdapter(noteAdapter);
    }

    private void filterNotes(String query) {
        try {
            List<Note> filtered = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                filtered = noteList.stream()
                        .filter(n -> TextUtils.isEmpty(query) ||
                                n.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                                n.getContent().toLowerCase().contains(query.toLowerCase()))
                        .toList();
            }

            assert filtered != null;
            noteAdapter.submitList(new ArrayList<>(filtered));
            binding.emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            Log.e(NOTE_LOG_TAG, "Filter error", e);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("CURRENT_QUERY", currentQuery);
        outState.putBoolean("IS_REVERSE_LAYOUT", isReverseLayout);
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
            isReverseLayout = !isReverseLayout;
            LinearLayoutManager manager = (LinearLayoutManager) binding.rvNotes.getLayoutManager();
            if (manager != null) {
                manager.setReverseLayout(isReverseLayout);
                manager.setStackFromEnd(isReverseLayout);
            }
        }
    }
}
