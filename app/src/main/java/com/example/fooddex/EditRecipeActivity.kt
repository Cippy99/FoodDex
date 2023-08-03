package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddex.databinding.ActivityEditProductBinding
import com.example.fooddex.databinding.ActivityEditRecipeBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecipeBinding
    private lateinit var tietCategory: TextInputEditText
    private lateinit var tietAmount: TextInputEditText
    private lateinit var recyclerView: RecyclerView
    private var selectedDate: LocalDate = LocalDate.now()
    private var recipeId: String? = null
    private var allIngredients = mutableListOf<Pair<Product,Int>>()
    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference

    // funzione che viene chiamata alla creazione della view dell'attività
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        dbReference = Firebase.database.reference
        recyclerView = binding.rvRecipes
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
        // salva la ricetta all'uscita dall'attività
        binding.topAppBar.setOnMenuItemClickListener{menuItem ->
            when(menuItem.itemId){
                R.id.done ->{
                    val name = binding.tietRecipeName.text.toString()
                    val category = binding.tietCategory.text.toString()
                    val portion = binding.tietRecipePortion.text.toString().toInt()
                    val udm = (binding.tilPortionUM.editText as AutoCompleteTextView).text.toString()
                    Log.d("Debug", "Saving Recipe")
                    saveRecipe(name, category, portion, udm, allIngredients)
                    true
                }
                else -> false
            }

        }
        recipeId = intent.getStringExtra("recipeId")
        if(!recipeId.isNullOrEmpty()){
            retrieveProductAndFillFields(recipeId!!)
        }


    }

    private fun retrieveProductAndFillFields(productId: String){
        val userId = auth.currentUser?.uid!!
        val userRef = dbReference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {
                        val recipeRef: DatabaseReference = dbReference.child("recipes").child(familyId).child(productId)

                        // Add a ChildEventListener to fetch all products from the database under the familyId node
                        recipeRef.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val recipe = snapshot.getValue(Recipe::class.java)
                                if (recipe != null){
                                    fillFields(recipe)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })

                    } else {
                        // Handle the case where familyId is empty or not available
                    }
                } else {
                    // Handle the case where familyId is not available in the database
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }

    private fun fillFields(recipe: Recipe) {
        binding.tietRecipeName.setText(recipe.name)
        binding.tietRecipePortion.setText(recipe.nOfPerson.toString())
        (binding.tilPortionUM.editText as AutoCompleteTextView).setText("Persone", false)
        allIngredients = recipe.ingredients!!
        updateRecyclerView()
    }

    private fun saveRecipe(name: String, category: String, portion: Int, udm: String, allIngredients: MutableList<Pair<Product,Int>>) {
        val userRef = dbReference.child("users").child(auth.currentUser?.uid!!)

        userRef.child("familyId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val familyId = snapshot.getValue(String::class.java)

                // FAMILY_CODE might be null if the "familyId" property doesn't exist in the database
                if (familyId != null) {
                    // Create a reference to the "products" node under the FAMILY_CODE
                    if(recipeId.isNullOrEmpty()){
                        Log.d("Debug", "New Recipe")

                        val recipeRef = dbReference.child("recipes").child(familyId).push()

                        // Get the unique ID generated by push() and set it in the product object
                        val recipeID = recipeRef.key

                        val product = Recipe(recipeID!!, name, category, portion, allIngredients)

                        // Save the product to the database using the productRef
                        recipeRef.setValue(product)
                    }
                    else{
                        Log.d("Debug", "Recipe Exists")

                        val recipeRef = dbReference.child("recipes").child(familyId).child(recipeId!!)
                        val product = Recipe(recipeId!!, name, category, portion, allIngredients)
                        recipeRef.setValue(product)
                    }

                    finish()
                } else {
                    // Handle the case when "familyId" doesn't exist in the database for the user
                    // You can show an error message or take appropriate action
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateRecyclerView() {
        // TODO
        allIngredients.sortBy { it.second }

    }

}

