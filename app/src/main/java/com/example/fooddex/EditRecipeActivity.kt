package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AutoCompleteTextView
import androidx.annotation.DrawableRes
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddex.databinding.ActivityEditRecipeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class EditRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditRecipeBinding
    private lateinit var recyclerView: RecyclerView
    private var recipeId: String? = null
    private var allIngredients = mutableListOf<Pair<Product, Double>>()
    //private val productsMap = mutableMapOf<String, Product>()
    @DrawableRes private var iconId: Int = IconData.iconList[0].iconId

    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference

    // funzione che viene chiamata alla creazione della view dell'attività
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        dbReference = Firebase.database.reference

        binding.ivIcon.setImageResource(iconId)

        recyclerView = binding.rvRecipes
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        // salva la ricetta all'uscita dall'attività
        binding.topAppBar.setOnMenuItemClickListener{menuItem ->
            when(menuItem.itemId){
                R.id.done ->{
                    val name = binding.tietRecipeName.text.toString()
                    val category = (binding.tilCategory.editText as AutoCompleteTextView).text.toString()
                    val nPeople = binding.tietNPeople.text.toString().toInt()

                    val ingredients = mutableListOf<IngredientWithAmount>()

                    for(ingredient in allIngredients){
                        ingredients.add(IngredientWithAmount(ingredient.first.id, ingredient.second))
                    }

                    saveRecipe(name, category, nPeople, iconId, ingredients)
                    true
                }
                else -> false
            }

        }

        //Icon Selector
        val iconPicker = IconPickerDialog(this, IconData.sortedIconList)
        binding.ivIcon.setOnClickListener{
            iconPicker.show { icon ->
                binding.ivIcon.setImageResource(icon.iconId)
                iconId = icon.iconId
            }
        }

        //Add Ingredient

        binding.btnAddIngredient.setOnClickListener {
            val newFragment = IngredientSelectionDialogFragment()
            newFragment.onIngredientSelected { product, amount ->
                allIngredients.add(Pair(product, amount))
                //productsMap[product.id] = product
                updateRecyclerView()
            }

            //Show fullscreen dialog
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction
                .add(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit()
        }



        recipeId = intent.getStringExtra("recipeId")
        if(!recipeId.isNullOrEmpty()){
            retrieveRecipesAndFillFields(recipeId!!)
        }


    }

    private fun retrieveRecipesAndFillFields(recipeId: String){
        val userId = auth.currentUser?.uid!!
        val userRef = dbReference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {
                        val recipeRef: DatabaseReference = dbReference.child("recipes").child(familyId).child(recipeId)

                        // Add a ChildEventListener to fetch all products from the database under the familyId node
                        recipeRef.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val recipe = snapshot.getValue(Recipe::class.java)
                                if (recipe != null){

                                    val productRef: DatabaseReference = dbReference.child("products").child(familyId)

                                    productRef.addListenerForSingleValueEvent(object: ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for(ingredient in recipe.ingredients){

                                                val productSnapshot = snapshot.child(ingredient.productId)
                                                val product = productSnapshot.getValue(Product::class.java)
                                                if (product != null) {
                                                    allIngredients.add(Pair(product, ingredient.amount))
                                                    //productsMap[product.id] = product
                                                }
                                            }
                                            fillFields(recipe)
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                        }

                                    })

                                }
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
                // Handle database error if needed
            }
        })
    }

    private fun fillFields(recipe: Recipe) {
        binding.tietRecipeName.setText(recipe.name)
        binding.tietNPeople.setText(recipe.nOfPeople.toString())
        (binding.tilCategory.editText as AutoCompleteTextView).setText(recipe.category, false)
        //allIngredients = recipe.ingredients
        binding.ivIcon.setImageResource(recipe.iconId)
        iconId = recipe.iconId
        updateRecyclerView()
    }

    private fun saveRecipe(name: String, category: String, nPeople: Int, @DrawableRes icon_id: Int, allIngredients: MutableList<IngredientWithAmount>) {
        val userRef = dbReference.child("users").child(auth.currentUser?.uid!!)

        userRef.child("familyId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val familyId = snapshot.getValue(String::class.java)

                // FAMILY_CODE might be null if the "familyId" property doesn't exist in the database
                if (familyId != null) {
                    // Create a reference to the "products" node under the FAMILY_CODE
                    if(recipeId.isNullOrEmpty()){

                        val recipeRef = dbReference.child("recipes").child(familyId).push()

                        // Get the unique ID generated by push() and set it in the product object
                        val recipeID = recipeRef.key

                        val product = Recipe(recipeID!!, name, category, nPeople, allIngredients, icon_id)

                        // Save the product to the database using the productRef
                        recipeRef.setValue(product)
                    }
                    else{

                        val recipeRef = dbReference.child("recipes").child(familyId).child(recipeId!!)
                        val product = Recipe(recipeId!!, name, category, nPeople, allIngredients, icon_id)
                        recipeRef.setValue(product)
                    }

                    finish()
                } else {
                    // Handle the case when "familyId" doesn't exist in the database for the user
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateRecyclerView() {

        val adapter = RecipeIngredientsAdapter(allIngredients)

        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

    }

}

