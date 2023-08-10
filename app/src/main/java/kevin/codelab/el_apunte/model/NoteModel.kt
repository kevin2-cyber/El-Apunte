package kevin.codelab.el_apunte.model

import com.google.firebase.Timestamp

data class Note(
    var title: String,
    var content: String,
    var color: Int,
    var timestamp: Timestamp
)