package com.kimikevin.el_apunte.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.kimikevin.el_apunte.R;
import com.kimikevin.el_apunte.databinding.ActivityEditBinding;
import com.kimikevin.el_apunte.model.entity.Note;

public class EditActivity extends AppCompatActivity {
    public static final String NOTE_ID = "note_id";
    public static final String NOTE_TITLE = "note_title";
    public static final String NOTE_CONTENT = "note_content";
    public static final String NOTE_TIME = "note_time";

    private ActivityEditBinding binding;
    private Note note;
    private String originalTitle;
    private String originalContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit);

        note = new Note();
        binding.setNote(note);
        binding.setHandler(new EditClickHandler(this));

        setupNoteFromIntent();
    }

    private void setupNoteFromIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(NOTE_ID)) {
            setTitle(R.string.edit_note);
            originalTitle = intent.getStringExtra(NOTE_TITLE);
            originalContent = intent.getStringExtra(NOTE_CONTENT);

            note.setTitle(originalTitle);
            note.setContent(originalContent);
        } else {
            setTitle(R.string.create_note);
            originalTitle = "";
            originalContent = "";
        }
    }

    public class EditClickHandler {
        private final Context context;

        public EditClickHandler(Context context) {
            this.context = context;
        }

        public void onSubmitButtonClicked(View view) {
            String title = note.getTitle() != null ? note.getTitle().trim() : "";
            String content = note.getContent() != null ? note.getContent().trim() : "";

            if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
                Toast.makeText(context, R.string.empty_note_error, Toast.LENGTH_LONG).show();
                return;
            }

            // Check if there are actual changes
            boolean hasChanges = !title.equals(originalTitle) || !content.equals(originalContent);

            if (!hasChanges) {
                Toast.makeText(context, R.string.no_changes_detected, Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED);
                finish();
                return;
            }

            try {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(NOTE_TITLE, title);
                resultIntent.putExtra(NOTE_CONTENT, content);
                resultIntent.putExtra(NOTE_TIME, note.getFormattedDate());

                setResult(RESULT_OK, resultIntent);
                finish();
            } catch (Exception e) {
                Toast.makeText(context, R.string.save_note_error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}