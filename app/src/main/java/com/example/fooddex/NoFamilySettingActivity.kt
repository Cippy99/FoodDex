package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.fooddex.databinding.ActivityNoFamilySettingBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
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
            val code = tietCode.text.toString().uppercase()
            if(code.isNullOrEmpty()){
                tilCode.error = getString(R.string.family_code_empty)
            }else if (code.length != 6){
                tilCode.error = getString(R.string.family_code_length_6)
            }
            else if(!runBlocking { exists(code) }){
                tilCode.error = getString(R.string.family_code_not_exists)
            }
            else{
                joinFamily(auth.currentUser!!.uid, code)
                finish()
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
                val familyCode = runBlocking { generateUniqueCode() }
                val family = Family(name, familyCode, creatorId , listOf(creatorId))

                dbReference.child("families").child(family.id).setValue(family)

                //Assign family to user
                dbReference.child("users").child(creatorId).child("familyId").setValue(family.id)

            }
        }


        setContentView(binding.root)
    }

    fun joinFamily(userId: String, familyId: String) {

        // Reference to the specific family using the provided familyId
        val membersRef = dbReference.child("families").child(familyId).child("members")

        // Reference to the user
        val userRef = dbReference.child("users").child(userId)

        // Check if the family with the provided familyId exists
        membersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    // Get the current list of members as a mutable list
                    val currentMembers: MutableList<String> = mutableListOf()
                    for (memberSnapshot in dataSnapshot.children) {
                        val memberId = memberSnapshot.getValue(String::class.java)
                        if (memberId != null) {
                            currentMembers.add(memberId)
                        }
                    }

                    // Add the user to the list of members
                    currentMembers.add(userId)

                    // Update the family's list of members with the new list
                    membersRef.setValue(currentMembers)


                } else {
                    Log.d("debug", "Family with ID $familyId does not exist.")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Log.d("debug", "Error joining family: ${databaseError.message}")
            }
        })

        userRef.child("familyId").setValue(familyId)
    }

    private suspend fun exists(code: String): Boolean {
        val famRef = dbReference.child("families")
        val snapshot = famRef.child(code.uppercase()).get().await()
        return snapshot.exists()
    }

    private suspend fun generateUniqueCode(): String {
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