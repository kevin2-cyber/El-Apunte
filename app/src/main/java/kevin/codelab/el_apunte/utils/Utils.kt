package kevin.codelab.el_apunte.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

 class Utils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

     fun getCollectionReferenceForNotes(): CollectionReference {
        val currentUser = FirebaseAuth.getInstance().currentUser

        return FirebaseFirestore.getInstance().collection("notes")
            .document(currentUser!!.uid).collection("my_notes")
    }

    fun timeStampToString(timestamp: Timestamp): String {
        return SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).format(timestamp.toDate())
    }
}