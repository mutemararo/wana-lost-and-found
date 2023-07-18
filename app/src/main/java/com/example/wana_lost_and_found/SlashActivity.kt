package com.example.wana_lost_and_found

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.wana_lost_and_found.ui.login_signup.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class SlashActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slash)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        Handler().postDelayed({
            if (firebaseAuth.currentUser == null){
                val intent = Intent(this@SlashActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                val intent = Intent(this@SlashActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 3000)
    }
}