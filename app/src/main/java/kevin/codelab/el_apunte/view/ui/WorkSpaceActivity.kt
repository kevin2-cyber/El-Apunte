package kevin.codelab.el_apunte.view.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import kevin.codelab.el_apunte.R
import kevin.codelab.el_apunte.databinding.ActivityWorkSpaceBinding
import kevin.codelab.el_apunte.model.Note
import kevin.codelab.el_apunte.utils.Utils

class WorkSpaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkSpaceBinding
    private var mImageButtonCurrentPaint: ImageButton? = null
    private var model: Note? = null
    private var isEditMode: Boolean = false
    private val etTitle = binding.etTitle
    private val etContent = binding.etContent
    private var utils: Utils? = null
    private lateinit var docId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWorkSpaceBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.red.setOnClickListener {
            paintClicked(it)
        }

        binding.amber.setOnClickListener {
            paintClicked(it)
        }

        binding.green.setOnClickListener {
            paintClicked(it)
        }

        binding.violet.setOnClickListener {
            paintClicked(it)
        }

        binding.indigo.setOnClickListener {
            paintClicked(it)
        }

        binding.backBtn.setOnClickListener {
            onSupportNavigateUp()
        }

        binding.saveBtn.setOnClickListener {
            saveNote()
        }

        val linearLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_colors)
        mImageButtonCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )

        val mImageButtonCurrentPaint = findViewById<ImageButton>(R.id.ib_pallet)

        // receive data
        var title = intent.getStringExtra("title").toString()
        var content = intent.getStringExtra("content").toString()
        docId = intent.getStringExtra("docId").toString()
        val color = intent.getIntExtra("color", model!!.color)

        if(docId.isNotEmpty()) {
            isEditMode = true
        }

        title = etTitle.text.toString()
        content = etContent.text.toString()


        mImageButtonCurrentPaint.setImageResource(color)
    }

    private fun saveNote() {
        val noteTitle = etTitle.text.toString()
        val noteContent = etContent.text.toString()
        val color = mImageButtonCurrentPaint!!.solidColor

        if (noteTitle.isEmpty() && noteContent.isEmpty()) {
            etTitle.error = "Title cannot be empty"
            etContent.error = "Please type something"
        }

        model?.title = noteTitle
        model?.content = noteContent
        model?.color = color
        model?.timestamp = Timestamp.now()

        model?.let { saveNoteToFirebase(it) }
    }

    private fun saveNoteToFirebase(model: Note) {
        val documentReference: DocumentReference = if (isEditMode) {
            // update the note
            utils!!.getCollectionReferenceForNotes().document(docId)
        } else {
            // create a new note
            utils!!.getCollectionReferenceForNotes().document()
        }

        documentReference.set(model)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    utils!!.showToast(this, "Note added successfully")
                    finish()
                } else utils!!.showToast(this, "Failed whilst adding note")
            }
    }

    private fun paintClicked(view: View) {
        Toast.makeText(this, "paint clicked", Toast.LENGTH_LONG).show()
        if (view !== mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_pressed
                )
            )

            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_normal
                )
            )

            mImageButtonCurrentPaint = view
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}