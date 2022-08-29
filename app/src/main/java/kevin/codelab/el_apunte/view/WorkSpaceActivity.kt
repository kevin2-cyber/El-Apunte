package kevin.codelab.el_apunte.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kevin.codelab.el_apunte.databinding.ActivityWorkSpaceBinding

class WorkSpaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkSpaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityWorkSpaceBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}