package com.example.fooddex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.fooddex.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var btnSignUp: Button
    private lateinit var btnLogin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var tietEmail: TextInputEditText
    private lateinit var tietPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        btnSignUp = binding.btnSignUp
        btnLogin = binding.btnLogin
        tietEmail = binding.tietEmail
        tietPassword = binding.tietPassword


        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
            finish()
        }

        btnLogin.setOnClickListener {
            val email = tietEmail.text.toString()
            val password = tietPassword.text.toString()

            login(email, password)
        }
    }

    private fun login(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    val intent = Intent(this@Login, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    Toast.makeText(this@Login, "Errore nel Login", Toast.LENGTH_LONG).show()
                }
            }
    }
}