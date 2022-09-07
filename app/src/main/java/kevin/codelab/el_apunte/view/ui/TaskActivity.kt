package kevin.codelab.el_apunte.view.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import kevin.codelab.el_apunte.R
import kevin.codelab.el_apunte.databinding.ActivityTaskBinding
import kevin.codelab.el_apunte.model.NoteModel
import kevin.codelab.el_apunte.view.adapters.RecyclerViewAdapter

class TaskActivity : AppCompatActivity() {

    // enable viewBinding
    private lateinit var binding: ActivityTaskBinding

    private lateinit var noteList: ArrayList<NoteModel>
    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTaskBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        TODO("Not yet implemented")
    }
}