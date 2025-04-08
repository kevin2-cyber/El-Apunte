package com.kimikevin.el_apunte.view;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.kimikevin.el_apunte.R;
import com.kimikevin.el_apunte.databinding.ActivityEditBinding;
import com.kimikevin.el_apunte.model.entity.Note;

public class EditActivity extends AppCompatActivity {
    private Note note;
    public static final String NOTE_ID = "note_id";
    public static final String NOTE_TITLE = "note_title";
    public static final String NOTE_CONTENT = "note_content";
    public static final String NOTE_TIME = "note_time";
    public static String NOTE_CHANGED = "note_changed";
    ActivityEditBinding binding;
    EditClickHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit);

        note = new Note();

        binding = DataBindingUtil.setContentView(this,R.layout.activity_edit);
        binding.setNote(note);

        handler = new EditClickHandler(this);
        binding.setHandler(handler);

        Intent i = getIntent();
        if (i.hasExtra(NOTE_ID)) {
            // RecyclerView item clicked
            setTitle("Edit Course");
            note.setTitle(i.getStringExtra(NOTE_TITLE));
            note.setContent(i.getStringExtra(NOTE_CONTENT));
        } else {
            // fab clicked
            setTitle("Create New Course");
        }
    }

    public class EditClickHandler {
        Context context;

        public EditClickHandler(Context context) {
            this.context = context;
        }

        public void onSubmitButtonClicked(View view) {
            if (note.getTitle() == null && note.getContent() == null) {
                Toast.makeText(context, "Title and content Cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent();
                intent.putExtra(NOTE_TITLE,note.getTitle());
                intent.putExtra(NOTE_CONTENT, note.getContent());
                intent.putExtra(NOTE_TIME, note.getFormattedDate());
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}

//public void onSubmitButtonClicked(View view) {
//    // Trim inputs to avoid whitespace-only entries
//    String title = note.getTitle() != null ? note.getTitle().trim() : "";
//    String content = note.getContent() != null ? note.getContent().trim() : "";
//
//    if (title.isEmpty() && content.isEmpty()) {
//        // Show error with better UX
//        Toast.makeText(context, "Please enter either a title or content", Toast.LENGTH_LONG).show();
//        return;
//    }
//
//    try {
//        Intent intent = new Intent();
//        // Only put non-empty values
//        if (!title.isEmpty()) intent.putExtra(NOTE_TITLE, title);
//        if (!content.isEmpty()) intent.putExtra(NOTE_CONTENT, content);
//
//        setResult(RESULT_OK, intent);
//        finish();
//
//    } catch (Exception e) {
//        Log.e(NOTE_LOG_TAG, "Error saving note", e);
//        Toast.makeText(context, "Failed to save note", Toast.LENGTH_SHORT).show();
//    }
//}
//    }
//        public void onSubmitButtonClicked(View view) {
//            // Get current values (with null protection and trimming)
//            String newTitle = note.getTitle() != null ? note.getTitle().trim() : "";
//            String newContent = note.getContent() != null ? note.getContent().trim() : "";
//
//            // Get original values (assuming they're stored somewhere)
//            String originalTitle = note.getTitle() != null ? note.getTitle().trim() : "";
//            String originalContent = note.getContent() != null ? note.getContent().trim() : "";
//
//            // Check if either title or content is empty (if both are required)
//            if (newTitle.isEmpty() && newContent.isEmpty()) {
//                Toast.makeText(context, "Please enter either a title or content", Toast.LENGTH_LONG).show();
//                return;
//            }
//
//            // Only proceed if there are actual changes
//            if (!newTitle.equals(originalTitle) || !newContent.equals(originalContent)) {
//                try {
//                    Intent intent = new Intent();
//
//                    // Only include changed values (or all if you prefer)
//                    if (!newTitle.equals(originalTitle)) {
//                        intent.putExtra(NOTE_TITLE, newTitle);
//                        note.setTitle(newTitle); // Update model if needed
//                    }
//
//                    if (!newContent.equals(originalContent)) {
//                        intent.putExtra(NOTE_CONTENT, newContent);
//                        note.setContent(newContent); // Update model if needed
//                    }
//
//                    // Add flag indicating changes were made
//                    intent.putExtra(NOTE_CHANGED, true);
//
//                    setResult(RESULT_OK, intent);
//                    finish();
//
//                } catch (Exception e) {
//                    Log.e(NOTE_LOG_TAG, "Error saving note", e);
//                    Toast.makeText(context, "Failed to save note", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                // No changes made
//                Toast.makeText(context, "No changes detected", Toast.LENGTH_SHORT).show();
//                setResult(RESULT_CANCELED);
//                finish();
//            }
//        }