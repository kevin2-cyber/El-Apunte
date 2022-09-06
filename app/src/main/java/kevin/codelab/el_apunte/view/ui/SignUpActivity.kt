package kevin.codelab.el_apunte.view.ui


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kevin.codelab.el_apunte.databinding.ActivitySignUpBinding
import java.util.*

class SignUpActivity : AppCompatActivity() {

    // enable viewBinding
    private lateinit var binding: ActivitySignUpBinding

    // FirebaseAuth
    private lateinit var auth: FirebaseAuth

    // progress bar
    private lateinit var bar: ProgressBar

    // user inputs
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var confPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        // configure progress bar
        bar = ProgressBar(this)
        bar.visibility = View.INVISIBLE

        // init firebase auth
        auth = FirebaseAuth.getInstance()


        binding.signUpBtn.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {

        // get data
        email = binding.etEmail.text.toString().trim()
        password = binding.etPass.text.toString().trim()
        confPassword = binding.etPassConf.text.toString().trim()

        // validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())

            // invalid email format
            binding.etEmail.error = "Invalid email format"
        else if (TextUtils.isEmpty(password))

            // password isn't entered
            binding.etPass.error = "Please enter your password"
        else if (password.length < 6)
            binding.etPass.error = "Please enter at least 6 characters long"
        else if (TextUtils.isEmpty(confPassword))
            binding.etPassConf.error = "Please confirm your password"
        else if (!Objects.equals(password, confPassword))
            Toast.makeText(this, "Password must be the same",
                Toast.LENGTH_LONG).show()
        else firebaseSignUp()

    }

    private fun firebaseSignUp() {}
}