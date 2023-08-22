package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.example.fooddex.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isUserPartOfFamily = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        //Fetch Information about user family
        attachListenerToUserFamily()

        val bottomNavigationView = binding.bottomNavigationView

        bottomNavigationView.setOnItemSelectedListener {
            changeFragment(it.itemId)
            true
        }

        //Set Initial Fragment
        if(isUserPartOfFamily){
            replaceFragment(InventoryFragment())
        }
        else{
            replaceFragment(NoFamilyFragment())
        }

        val view = binding.root
        setContentView(view)
    }

    private fun changeFragment(itemId: Int) {
        when (itemId) {
            R.id.inventoryFragment -> {
                if (isUserPartOfFamily) {
                    replaceFragment(InventoryFragment())
                } else {
                    replaceFragment(NoFamilyFragment())
                }

            }

            R.id.mealsFragment -> {
                if (isUserPartOfFamily) {
                    replaceFragment(MealsFragment())
                } else {
                    replaceFragment(NoFamilyFragment())
                }

            }

            R.id.recipesFragment -> {
                if (isUserPartOfFamily) {
                    replaceFragment(RecipesFragment())
                } else {
                    replaceFragment(NoFamilyFragment())
                }

            }

            R.id.settingsFragment -> replaceFragment(SettingsFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, fragment)
            commit()
        }
    }

    private fun attachListenerToUserFamily(){
        val dbRef = Firebase.database.reference
        val auth = Firebase.auth

        dbRef.child("users").child(auth.currentUser!!.uid).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("Family", "$snapshot")
                val familyId = snapshot.child("familyId").getValue(String::class.java)

                isUserPartOfFamily = familyId != null
                val currentFragmentId = binding.bottomNavigationView.selectedItemId
                if(currentFragmentId != R.id.settingsFragment){
                    changeFragment(currentFragmentId)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                isUserPartOfFamily = false
            }

        })
    }


}