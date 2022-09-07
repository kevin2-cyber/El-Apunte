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
import kevin.codelab.el_apunte.utils.Utils

class RecyclerViewAdapter(options: FirestoreRecyclerOptions<NoteModel>) :
    FirestoreRecyclerAdapter<NoteModel, RecyclerViewAdapter.RecyclerViewHolder>(options) {



    class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

         var note: CardView = itemView.findViewById(R.id.card_view)
         var title: TextView = itemView.findViewById(R.id.et_title)
         var date: TextView = itemView.findViewById(R.id.tv_date)
         var colorPallet: ImageButton = itemView.findViewById(R.id.ib_pallet)
         var deleteBtn: ImageButton = itemView.findViewById(R.id.ib_delete)

        val view: RecyclerView = itemView.findViewById(R.id.recyclerView)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int, model: NoteModel) {
        holder.title.text = model.title
        holder.date.text = Utils.timeStampToString(model.timestamp)
    }
}