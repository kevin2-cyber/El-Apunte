package kevin.codelab.el_apunte.view.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kevin.codelab.el_apunte.R
import kevin.codelab.el_apunte.databinding.ActivityWorkSpaceBinding
import kevin.codelab.el_apunte.model.NoteModel

class WorkSpaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkSpaceBinding
    private var mImageButtonCurrentPaint: ImageButton? = null
    private var model: NoteModel? = null
    private var isEditMode: Boolean = false

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

        var etTitle = binding.etTitle
        var etContent = binding.etContent
        val mImageButtonCurrentPaint = findViewById<ImageButton>(R.id.ib_pallet)

        // receive data
        var title = intent.getStringExtra("title")
        var content = intent.getStringExtra("content")
        var docId = intent.getStringExtra("docId")
        var color = intent.getIntExtra("color", model!!.color)

        if(docId != null && docId.isNotEmpty()) {
            isEditMode = true
        }

        etTitle.text = title
        etContent.text = content

        mImageButtonCurrentPaint.setImageResource(color)
    }

    private fun saveNote() {
        TODO("Not yet implemented")
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