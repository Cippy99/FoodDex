package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.fooddex.databinding.ActivityNoFamilySettingBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.random.Random

class NoFamilySettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoFamilySettingBinding
    private lateinit var tietCode: TextInputEditText
    private lateinit var tietName: TextInputEditText
    private lateinit var tilCode: TextInputLayout
    private lateinit var tilName: TextInputLayout
    private lateinit var btnJoin: Button
    private lateinit var btnCreate: Button
    private lateinit var auth:FirebaseAuth
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoFamilySettingBinding.inflate(layoutInflater)
        tietCode = binding.tietFamilyCode
        tietName = binding.tietFamilyName
        tilCode = binding.tilFamilyCode
        tilName = binding.tilFamilyName
        btnJoin = binding.btnJoin
        btnCreate = binding.btnCreate

        auth = Firebase.auth
        dbReference = Firebase.database.reference

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        btnJoin.setOnClickListener {
            val code = tietCode.text.toString()
            if(code.isNullOrEmpty()){
                tilCode.error = getString(R.string.family_code_empty)
            }else if (code.length != 6){
                tilCode.error = getString(R.string.family_code_length_6)
            }
            else if(!exists(code)){
                tilCode.error = getString(R.string.family_code_not_exists)
            }
        }

        btnCreate.setOnClickListener {
            val name = tietName.text.toString()
            if(name.isNullOrEmpty()){
                tilName.error = getString(R.string.empty_name)
            }
            else{
                //Create new Family and save to db
                val creatorId = auth.currentUser?.uid!!
                val family = Family(name, generateUniqueCode(), creatorId , listOf(creatorId))

                dbReference.child("families").child(family.id).setValue(family)

                //Assign family to user
                dbReference.child("users").child(creatorId).child("familyId").setValue(family.id)
            }
        }


        setContentView(binding.root)
    }

    private fun exists(code: String): Boolean {
        return false
    }

    private fun generateUniqueCode(): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val codeLength = 6
        val random = Random.Default
        val stringBuilder = StringBuilder(codeLength)

        do {
            stringBuilder.clear()
            repeat(codeLength) {
                val randomIndex = random.nextInt(characters.length)
                val randomChar = characters[randomIndex]
                stringBuilder.append(randomChar)
            }

            val code = stringBuilder.toString()
        } while (exists(code))

        return stringBuilder.toString()
    }


}