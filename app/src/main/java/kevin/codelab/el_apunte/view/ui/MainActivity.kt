package kevin.codelab.el_apunte.view.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import kevin.codelab.el_apunte.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(binding.root)

        // init firebase
        auth = FirebaseAuth.getInstance()

        binding.welcomeBtn.setOnClickListener {
            startActivity(
                Intent(this, SignUpActivity::class.java)
            )
            finish()
        }
    }
}