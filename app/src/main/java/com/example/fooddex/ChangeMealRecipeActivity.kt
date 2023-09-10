package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.fooddex.databinding.ActivityChangeMealRecipeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ChangeMealRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeMealRecipeBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference

    private var recipesList = mutableListOf<Recipe>()
    private lateinit var adapter: SimpleRecipeAdapter
    private lateinit var mealDate: String
    private lateinit var mealId: String
    private lateinit var newRecipe: Recipe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeMealRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        dbReference = Firebase.database.reference

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.rv_spacing_8dp
            )!!
        )


        binding.rvRecipes.addItemDecoration(itemDecorator)

        var previousRecipeId = intent.getStringExtra("recipeId")!!
        mealDate = intent.getStringExtra("mealDate")!!
        mealId = intent.getStringExtra("mealId")!!

        adapter = SimpleRecipeAdapter(recipesList, this, previousRecipeId)
        binding.rvRecipes.adapter = adapter

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.done -> {
                    saveChangedRecipe(binding.chkNotification.isChecked)
                    finish()
                    true
                }

                else -> false
            }

        }

        retrieveRecipesFromDb()

    }

    private fun retrieveRecipesFromDb() {
        val userId = auth.currentUser?.uid!!
        val userRef = dbReference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {

                        //Fetch Recipes
                        val recipesRef: DatabaseReference =
                            dbReference.child("recipes").child(familyId)

                        // Add a ChildEventListener to fetch all recipes from the database under the familyId node
                        recipesRef.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {
                                val recipe = snapshot.getValue(Recipe::class.java)
                                recipe?.let {
                                    recipesList.add(it)
                                    UpdateRecyclerView()
                                }
                            }

                            override fun onChildChanged(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {
                                val recipe = snapshot.getValue(Recipe::class.java)
                                recipe?.let {
                                    val index = recipesList.indexOfFirst { p -> p.id == it.id }
                                    if (index >= 0) {
                                        recipesList[index] = it
                                        UpdateRecyclerView()
                                    }
                                }
                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {
                                val recipe = snapshot.getValue(Recipe::class.java)
                                recipe?.let {
                                    recipesList.removeAll { p -> p.id == it.id }
                                    UpdateRecyclerView()
                                }
                            }

                            override fun onChildMoved(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {

                            }

                            override fun onCancelled(error: DatabaseError) {

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
                // Handle database error
            }
        })
    }

    private fun saveChangedRecipe(notifyFamily: Boolean) {

        newRecipe = adapter.selectedRecipe!!

        if (newRecipe.id != null) {
            val userId = auth.currentUser?.uid!!
            val userRef = dbReference.child("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChild("familyId")) {
                        val familyId = snapshot.child("familyId").value as String
                        if (familyId.isNotEmpty()) {

                            val mealRef: DatabaseReference = dbReference.child("meals")
                                .child(familyId).child(mealDate).child(mealId)

                            mealRef.child("recipeId").setValue(newRecipe.id)

                            if (notifyFamily) {
                                notifyFamily()
                            }

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
    }

    private fun UpdateRecyclerView() {
        adapter.notifyDataSetChanged()
    }

    private fun notifyFamily() {
        val title = "Cambio Ricetta"

        val date = LocalDate.ofEpochDay(mealDate.toLong())

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy")
        val body = "Nuova ricetta per il giorno ${date.format(formatter)}: ${newRecipe.name}"

        NotificationSender.notifyFamily(title, body)


    }

}