package kevin.codelab.el_apunte.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kevin.codelab.el_apunte.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    // enable viewBinding
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}