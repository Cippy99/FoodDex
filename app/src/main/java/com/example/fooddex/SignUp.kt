package com.example.fooddex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.fooddex.databinding.ActivitySignUpBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var tietEmail: TextInputEditText
    private lateinit var tietName: TextInputEditText
    private lateinit var tietPassword: TextInputEditText
    private lateinit var btnSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        tietEmail = binding.tietEmail
        tietName = binding.tietName
        tietPassword = binding.tietPassword
        btnSignUp = binding.btnSignUp

        btnSignUp.setOnClickListener {
            val name = tietName.text.toString()
            val email = tietEmail.text.toString()
            val password = tietPassword.text.toString()

            clearErrors()

            if (validate(name, email, password)){
                createAccount(name, email, password)
            }
        }
    }

    private fun createAccount(name:String, email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful){
                    addUserToDatabase(name, email, auth.currentUser?.uid!!)

                    val intent = Intent(this@SignUp, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    Toast.makeText(this@SignUp, "Errore nella Registrazione", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String) {
        dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("users").child(uid).setValue(User(name, email, uid))
    }

    private fun validate(name: String?, email: String?, password: String?): Boolean{
        var ok = true

        if (name.isNullOrEmpty()){
            ok = false
            binding.tilName.error = getString(R.string.empty_name)
        }

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

    private fun clearErrors() {
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilName.error = null
    }
}