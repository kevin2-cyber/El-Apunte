package com.kimikevin.elapunte.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.kimikevin.elapunte.R;
import com.kimikevin.elapunte.databinding.ActivityEditBinding;
import com.kimikevin.elapunte.model.entity.Note;

public class EditActivity extends AppCompatActivity {
    private Note note;
    public static final String NOTE_ID = "note_id";
    public static final String NOTE_TITLE = "note_title";
    public static final String NOTE_CONTENT = "note_content";
   ActivityEditBinding binding;
   EditClickHandler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
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
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}