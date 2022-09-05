package kevin.codelab.el_apunte.view.ui


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import com.google.firebase.auth.FirebaseAuth
import kevin.codelab.el_apunte.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    // enable viewBinding
    private lateinit var binding: ActivitySignUpBinding

    // FirebaseAuth
    private lateinit var auth: FirebaseAuth

    // progress bar
    private lateinit var bar: ProgressBar

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var confPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        binding.tvSignInActivity.setOnClickListener {
            firebaseSignUp()
        }
    }

    private fun firebaseSignUp() {}
}