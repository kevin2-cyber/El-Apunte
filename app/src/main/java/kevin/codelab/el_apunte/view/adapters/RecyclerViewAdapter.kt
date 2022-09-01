package kevin.codelab.el_apunte.view.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kevin.codelab.el_apunte.R
import kevin.codelab.el_apunte.model.NoteModel

class RecyclerViewAdapter(options: FirestoreRecyclerOptions<NoteModel>) :
    FirestoreRecyclerAdapter<NoteModel, RecyclerViewAdapter.RecyclerViewHolder>(options) {



    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        val view: RecyclerView = itemView.findViewById(R.id.recyclerView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int, model: NoteModel) {
        TODO("Not yet implemented")
    }
}