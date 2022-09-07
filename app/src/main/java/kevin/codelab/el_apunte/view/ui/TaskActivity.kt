package kevin.codelab.el_apunte.view.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kevin.codelab.el_apunte.R
import kevin.codelab.el_apunte.databinding.ActivityTaskBinding
import kevin.codelab.el_apunte.model.NoteModel
import kevin.codelab.el_apunte.utils.Utils
import kevin.codelab.el_apunte.view.adapters.RecyclerViewAdapter

class TaskActivity : AppCompatActivity() {

    // enable viewBinding
    private lateinit var binding: ActivityTaskBinding

    private lateinit var noteList: ArrayList<NoteModel>
    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private var utils: Utils? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTaskBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val query: Query? = utils?.getCollectionReferenceForNotes()?.orderBy("timestamp", Query.Direction.DESCENDING)
        val options: FirestoreRecyclerOptions<NoteModel> = query?.let {
            FirestoreRecyclerOptions.Builder<NoteModel>()
                .setQuery(it, NoteModel::class.java).build()
        } as FirestoreRecyclerOptions<NoteModel>

        val gridLayoutManager: GridLayoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = gridLayoutManager

        recyclerViewAdapter = RecyclerViewAdapter(this, noteList, options)
        recyclerView.adapter = recyclerViewAdapter
    }

    override fun onStart() {
        super.onStart()
        recyclerViewAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        recyclerViewAdapter.stopListening()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        recyclerViewAdapter.notifyDataSetChanged()
    }
}