package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.fooddex.databinding.ActivityFamilySettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FamilySettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFamilySettingsBinding
    private lateinit var tvFamilyName: TextView
    private lateinit var tvFamilyCode: TextView
    private lateinit var tvFamilyNumMembers: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFamilySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        dbReference = Firebase.database.reference

        tvFamilyName = binding.tvFamilyName
        tvFamilyCode = binding.tvFamilyCode
        tvFamilyNumMembers = binding.tvFamilyNumMembers

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        binding.btnLeave.setOnClickListener {
            leaveFamily(auth.currentUser!!.uid)
            //val intent = Intent(this, MainActivity::class.java)
            finish()
        }

        fetchFamilyInfo { family ->
            if(family != null){
                tvFamilyName.text = "${getString(R.string.family)} ${family.name}"
                tvFamilyCode.text = "${getString(R.string.code)}: ${family.id}"
                tvFamilyNumMembers.text = "${getString(R.string.members)}: ${family.members.size}"
            }
        }


    }

    private fun fetchFamilyInfo(callback: (Family?) -> Unit) {
        val userRef = dbReference.child("users").child(auth.currentUser?.uid!!)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String

                    // Retrieve the family information using the familyId
                    val familyRef = dbReference.child("families").child(familyId)
                    familyRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val family = snapshot.getValue(Family::class.java)
                                callback(family)
                            } else {
                                callback(null)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            callback(null)
                        }
                    })
                } else {
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    private fun leaveFamily(userId: String) {
        // Reference to the user
        val userRef = dbReference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val familyId = snapshot.child("familyId").getValue(String::class.java)

                if(familyId != null){
                    //Reference to family members
                    val membersRef = dbReference.child("families").child(familyId).child("members")

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
                                // Remove the user to the list of members
                                currentMembers.remove(userId)

                                // Update the family's list of members with the new list
                                membersRef.setValue(currentMembers)

                                //Remove familyId from user
                                userRef.child("familyId").removeValue()


                            } else {
                                Log.d("debug", "Family with ID $familyId does not exist.")
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle error
                            Log.d("debug", "Error leaving family: ${databaseError.message}")
                        }
                    })
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}