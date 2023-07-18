package com.example.wana_lost_and_found.ui.login_signup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.wana_lost_and_found.databinding.ActivityRegisterBinding
import com.example.wana_lost_and_found.utils.makeToast
import com.example.wana_lost_and_found.utils.redirectToLogin
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity(){

    private val binding: ActivityRegisterBinding by lazy{
        ActivityRegisterBinding.inflate(layoutInflater)
    }
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.run {
            cancelSignupButton.setOnClickListener {
                finish()
            }

                signupButton.setOnClickListener {
                    if (!(signupEmailEditText.text.isNullOrEmpty() &&
                                signupPasswordEditText.text.isNullOrEmpty() &&
                                signupConfirmPasswordEditText.text.isNullOrEmpty())){
                        if (signupPasswordEditText.text?.trim().toString() == signupConfirmPasswordEditText.text?.trim().toString()){
                            registerEmailWithPassword(signupEmailEditText.text?.trim().toString(), signupPasswordEditText.text?.trim().toString())
                        }else{
                            makeToast(this@RegisterActivity, "passwords don`t match")
                        }

                    }else{
                        makeToast(this@RegisterActivity, "enter all fields")
                    }

                }

        }
    }

    private fun showProgress(){
        binding.progressRegistration.visibility = View.VISIBLE
    }

    private fun hideProgress(){
        binding.progressRegistration.visibility = View.GONE
    }


    private fun registerEmailWithPassword(email: String, password: String) {
        showProgress()
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful){
                sendVerificationEmail()
                firebaseAuth.signOut()
                hideProgress()
                finish()
                makeToast(this@RegisterActivity, "Check your email for verification email")
            }
        }.addOnCanceledListener {
            makeToast(this@RegisterActivity, "Registration canceled")

        }.addOnFailureListener {
            makeToast(this@RegisterActivity, "Failed to register user")
        }
    }

    private fun sendVerificationEmail() {
        firebaseAuth.currentUser!!.sendEmailVerification().addOnSuccessListener {
            makeToast(this@RegisterActivity, "sent verification email")
        }
            .addOnFailureListener {
                makeToast(this@RegisterActivity, "failed to send email")
            }
    }

}