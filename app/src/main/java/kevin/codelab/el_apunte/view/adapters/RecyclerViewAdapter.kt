package kevin.codelab.el_apunte.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kevin.codelab.el_apunte.R
import kevin.codelab.el_apunte.model.NoteModel

class RecyclerViewAdapter(options: FirestoreRecyclerOptions<NoteModel>) :
    FirestoreRecyclerAdapter<NoteModel, RecyclerViewAdapter.RecyclerViewHolder>(options) {



    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        private var note: CardView? = null
        private var title: TextView? = null
        private var date: TextView? = null
        private var colorPallet: ImageButton? = null
        private var deleteBtn: ImageButton? = null

        val view: RecyclerView = itemView.findViewById(R.id.recyclerView)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int, model: NoteModel) {

    }
}