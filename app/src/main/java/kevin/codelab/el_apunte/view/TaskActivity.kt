package kevin.codelab.el_apunte.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kevin.codelab.el_apunte.databinding.ActivityHomeBinding

class TaskActivity : AppCompatActivity() {

    // enable viewBinding
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}