package kevin.codelab.el_apunte.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kevin.codelab.el_apunte.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    // enable viewBinding
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}