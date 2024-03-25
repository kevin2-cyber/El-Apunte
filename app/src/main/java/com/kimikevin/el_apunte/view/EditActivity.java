package com.kimikevin.el_apunte.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.kimikevin.el_apunte.R;
import com.kimikevin.el_apunte.databinding.ActivityEditBinding;
import com.kimikevin.el_apunte.model.entity.Note;

public class EditActivity extends AppCompatActivity {
    private Note note;
    public static final String NOTE_ID = "note_id";
    public static final String NOTE_TITLE = "note_title";
    public static final String NOTE_CONTENT = "note_content";
    ActivityEditBinding binding;
    EditClickHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}