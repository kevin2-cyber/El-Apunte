package kevin.codelab.el_apunte.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kevin.codelab.el_apunte.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    // enable viewBinding
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // init firebase
        var auth = FirebaseAuth.getInstance()


        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            firebaseLogin()
        }
    }

    private fun firebaseLogin() {
        TODO("Not yet implemented")
    }
}