package kevin.codelab.el_apunte.view


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kevin.codelab.el_apunte.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    // enable viewBinding
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var auth = FirebaseAuth.getInstance()


        binding.tvSignInActivity.setOnClickListener {
            firebaseSignUp()
        }
    }

    private fun firebaseSignUp() {

    }
}