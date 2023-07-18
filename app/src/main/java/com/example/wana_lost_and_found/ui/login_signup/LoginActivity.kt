package com.example.wana_lost_and_found.ui.login_signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.wana_lost_and_found.MainActivity
import com.example.wana_lost_and_found.R
import com.example.wana_lost_and_found.databinding.ActivityLoginBinding
import com.example.wana_lost_and_found.utils.makeToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private val binding : ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var authStateListener : FirebaseAuth.AuthStateListener

    private val googleSignInIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        if (it.resultCode == RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleTask(task)
        }
    }

    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleClient : GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(R.string.default_web_client_id.toString())
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(this@LoginActivity, gso)

        binding.run {
            registerWithEmailButton.setOnClickListener {
                val signupIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(signupIntent)
            }

            loginButton.setOnClickListener {
                if (!(loginEmailEditText.text.isNullOrEmpty() && loginPasswordEditText.text.isNullOrEmpty())){
                    loginUserWithEmailAndPassword(loginEmailEditText.text?.trim().toString(),
                        loginPasswordEditText.text?.trim().toString())
                }else{
                    makeToast(this@LoginActivity, "enter email and password")
                }
            }
            loginWithGoogleButton.setOnClickListener {
                signinWithGoogle()
            }
        }

        setupFirebaseAuth()
    }

    private fun signinWithGoogle(){
        val googleIntent = googleClient.signInIntent
        googleSignInIntent.launch(googleIntent)
    }
    private fun showProgress(){
        binding.progressLogin.visibility = View.VISIBLE
    }
    private fun hideProgress(){
        binding.progressLogin.visibility = View.GONE
    }

    private fun loginUserWithEmailAndPassword(email: String, password: String){
        showProgress()
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{signinTask ->
                    if (firebaseAuth.currentUser?.isEmailVerified == true){
                        if (signinTask.isSuccessful){
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            hideProgress()
                            finish()
                        }else{
                            Log.d("login process", "not successful")
                            makeToast(this@LoginActivity, "process canceled")
                        }
                    }else{
                        makeToast(this@LoginActivity, "check verification email in inbox")
                    }
                }
                    .addOnCanceledListener {
                        Log.d("login process", "process canceled")
                        makeToast(this@LoginActivity, "Process canceled, try again")
                    }
                    .addOnFailureListener { e ->
                        Log.d("login process", "failed login${e.message} because ${e.cause}")
                        makeToast(this@LoginActivity, "Failed to login user")
                    }



    }

    private fun handleTask(task: Task<GoogleSignInAccount>){
        val account : GoogleSignInAccount? = task.result
        if (account != null){
            updateUi(account)
        }else{
            makeToast(this@LoginActivity, "not signed in")
        }
    }

    private fun updateUi(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener{
            if (it.isSuccessful){
                val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(mainIntent)
            }else{
                makeToast(this@LoginActivity, "failed to sign in this email")
            }
        }

    }

    private fun setupFirebaseAuth(){

        authStateListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser

            if(user != null){
                if(user.isEmailVerified){
                    val mainIntent = Intent(this@LoginActivity, MainActivity::class.java)
                    mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(mainIntent)
                    finish()

                }else{
                    makeToast(this@LoginActivity, "check email for verification email")
                }
            }else{
                Log.d("login activity", "setupFirebaseAuth: user unknown")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

}