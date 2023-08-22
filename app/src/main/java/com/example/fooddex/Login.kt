package com.example.fooddex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.fooddex.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


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

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null){
            goToMainActivity()
        }
    }

    private fun login(email: String, password: String){
        clearErrors()
        if (validate(email, password)){
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful){
                        getFCMTokenAndRegister()
                        goToMainActivity()
                    }
                    else{
                        Toast.makeText(this@Login, "Errore nel Login, controlla le credenziali", Toast.LENGTH_LONG).show()
                    }
                }
        }

    }

    private fun getFCMTokenAndRegister(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener{ task ->
            if(task.isSuccessful){
                var token = task.result
                Log.d("My Token", token)
                registerFCMToken(token)
            }
        }
    }

    private fun registerFCMToken(token: String) {
        val dbRef = Firebase.database.reference

        val uid = auth.currentUser!!.uid

        val userFcmTokenRef = dbRef.child("fcm_tokens").child(uid)

        userFcmTokenRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val tokenExists = snapshot.children.any { it.getValue(String::class.java) == token}

                if(!tokenExists){
                    // Push a new FCM token to the user's tokens
                    val newTokenRef = userFcmTokenRef.push()

                    // Set the FCM token value
                    newTokenRef.setValue(token)
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
    }

    private fun goToMainActivity(){
        val intent = Intent(this@Login, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun validate(email: String?, password: String?): Boolean{
        var ok = true
        if (email.isNullOrEmpty()){
            ok = false
            binding.tilEmail.error = getString(R.string.empty_email)
        }

        if (password.isNullOrEmpty()){
            ok = false
            binding.tilPassword.error = getString(R.string.empty_password)
        }

        return ok
    }

}