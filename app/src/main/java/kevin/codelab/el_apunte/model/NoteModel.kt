package kevin.codelab.el_apunte.model

import com.google.firebase.Timestamp

data class NoteModel(
    val title: String,
    val content: String,
    val color: Int,
    val timestamp: Timestamp
)