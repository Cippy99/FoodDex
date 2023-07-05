package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.fooddex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val bottomNavigationView = binding.bottomNavigationView

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.inventoryFragment -> replaceFragment(InventoryFragment())
                R.id.mealsFragment -> replaceFragment(MealsFragment())
                R.id.recipesFragment -> replaceFragment(RecipesFragment())
                R.id.settingsFragment -> replaceFragment(SettingsFragment())
            }
            true
        }

        val view = binding.root
        setContentView(view)
    }

    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragmentContainerView, fragment)
            commit()
        }
    }

}