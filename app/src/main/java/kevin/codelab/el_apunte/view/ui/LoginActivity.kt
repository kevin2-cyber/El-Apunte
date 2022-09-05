package kevin.codelab.el_apunte.view.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kevin.codelab.el_apunte.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    // enable viewBinding
    private lateinit var binding: ActivityLoginBinding
    // firebase auth
    private lateinit var auth: FirebaseAuth
    private lateinit var bar: ProgressBar

    var email: String = ""
    var password: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // init firebase
        auth = FirebaseAuth.getInstance()
        checkUser()



        // handle click, sign up
        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // handle click, login
        binding.loginBtn.setOnClickListener {
            // before logging in validate data
            validateData()
        }
    }

    private fun validateData(){
        // get data
        email = binding.etEmail.text.toString().trim()
        password = binding.etPass.text.toString().trim()

        // validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            // invalid email format
            binding.etEmail.error = "Invalid Error format"
        } else if (TextUtils.isEmpty(password)) {

            // no password entered
            binding.etPass.error = "No password entered"
        } else {

            // data is validated, begin login
            firebaseLogin()
        }
    }

    private fun checkUser() {

        // if user is already logged in go to Task activity
        // get current user
        val user: FirebaseUser? = auth.currentUser
        if (user != null){

            // user is already logged in
            startActivity(Intent(this, TaskActivity::class.java))
        }
    }

    private fun firebaseLogin() {

        // show progress
        bar.visibility = View.VISIBLE
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                // login successful
                bar.visibility = View.GONE

                // get user info
                val user: FirebaseUser? = auth.currentUser
                val email: String? = user!!.email
                Toast.makeText(this, "Logged in as $email",
                         Toast.LENGTH_LONG).show()

                // open profile
                val intent = Intent(this, TaskActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {

                // login failed
                bar.visibility = View.GONE
                Toast.makeText(this, "Login failed due to ${it.message}",
                         Toast.LENGTH_LONG).show()
            }
    }
}