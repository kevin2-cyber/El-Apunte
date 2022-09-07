package kevin.codelab.el_apunte.view.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import kevin.codelab.el_apunte.databinding.ActivityWorkSpaceBinding

class WorkSpaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkSpaceBinding
    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWorkSpaceBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    private fun paintClicked(view: View) {
        Toast.makeText(this, "paint clicked", Toast.LENGTH_LONG).show()
    }
}