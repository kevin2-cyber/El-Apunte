package kevin.codelab.el_apunte

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kevin.codelab.el_apunte.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}