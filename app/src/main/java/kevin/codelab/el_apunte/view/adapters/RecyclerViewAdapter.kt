package kevin.codelab.el_apunte.view.adapters

import android.content.Context
import android.content.Intent
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
import kevin.codelab.el_apunte.model.Note
import kevin.codelab.el_apunte.utils.Utils
import kevin.codelab.el_apunte.view.ui.WorkSpaceActivity

class RecyclerViewAdapter(context: Context, noteList: ArrayList<Note>, options: FirestoreRecyclerOptions<Note>,) :
    FirestoreRecyclerAdapter<Note, RecyclerViewAdapter.RecyclerViewHolder>(options) {

    private lateinit var context: Context
    private lateinit var noteList: ArrayList<Note>



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

    override fun getItemCount(): Int {
        return noteList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int, model: Note) {
        val utils: Utils? = null
        holder.title.text = model.title
        holder.note.cardElevation = 0.5F
        holder.date.text = utils?.timeStampToString(model.timestamp)
        holder.colorPallet.setImageResource(model.color)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, WorkSpaceActivity::class.java)
            intent.putExtra("title", model.title)
            intent.putExtra("content", model.content)
            intent.putExtra("color", model.color)
            val docId: String = this.snapshots.getSnapshot(position).id
            intent.putExtra("docId", docId)
            context.startActivity(intent)
        }
    }
}