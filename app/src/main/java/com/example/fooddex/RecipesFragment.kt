package com.example.fooddex

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.example.fooddex.databinding.FragmentRecipesBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
class RecipesFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentRecipesBinding?= null
    private var recipesList = mutableListOf<Recipe>()
    private var filteredRecipesList = mutableListOf<Recipe>()
    private var productList = mutableListOf<Product>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var dbref: DatabaseReference

    private var filters =  mutableMapOf<String, Boolean>()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRecipesBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // vado a prendere le informazioni necessarie da Firebase
        // auth inizializzato come variabile privata
        auth = FirebaseAuth.getInstance()
        dbref = Firebase.database.reference

        resetFilters()
        filteredRecipesList.addAll(recipesList)

        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.rv_spacing_8dp
            )!!
        )

        recyclerView = binding.rvRecipes
        recyclerView.addItemDecoration(itemDecorator)

        adapter = RecipeAdapter(filteredRecipesList, requireContext())
        recyclerView.adapter = adapter

        retrieveRecipesAndProductsFromDb()

        // alla pressione del bottone apre l'attività di editing/aggiunta ricetta
        binding.fabAddRecipe.setOnClickListener {
            val intent = Intent(activity, EditRecipeActivity::class.java)
            startActivity(intent)
        }

        //Filter Dialog
        binding.btnFilter.setOnClickListener {
            val filterDialog = FilterDialog(
                requireActivity(),
                {f -> filters = f.toMutableMap()
                    filterListAndUpdateRecyclerView()},
                {resetFilters()
                    filterListAndUpdateRecyclerView()},
                filters)

            filterDialog.show()
        }
    }

    // importa le ricette dal db
    // funziona tramite gli event listener che notificano quando c'è un cambiamento
    private fun retrieveRecipesAndProductsFromDb() {
        val userId = auth.currentUser?.uid!!
        val userRef = dbref.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {

                        //Fetch Recipes
                        val recipesRef: DatabaseReference = dbref.child("recipes").child(familyId)

                        // Add a ChildEventListener to fetch all recipes from the database under the familyId node
                        recipesRef.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                val recipe = snapshot.getValue(Recipe::class.java)
                                recipe?.let {
                                    it.canBeCooked = it.canBeCooked(productList)
                                    recipesList.add(it)
                                    filterListAndUpdateRecyclerView()
                                }
                            }

                            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                                val recipe = snapshot.getValue(Recipe::class.java)
                                recipe?.let {
                                    val index = recipesList.indexOfFirst { p -> p.id == it.id }
                                    if (index >= 0) {
                                        it.canBeCooked = it.canBeCooked(productList)
                                        recipesList[index] = it
                                        filterListAndUpdateRecyclerView()
                                    }
                                }
                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {
                                val recipe = snapshot.getValue(Recipe::class.java)
                                recipe?.let {
                                    recipesList.removeAll { p -> p.id == it.id }
                                    filterListAndUpdateRecyclerView()
                                }
                            }

                            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })

                        //Fetch Products
                        val productsRef: DatabaseReference = dbref.child("products").child(familyId)

                        // Add a ChildEventListener to fetch all products from the database under the familyId node
                        productsRef.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                val product = snapshot.getValue(Product::class.java)
                                product?.let {
                                    productList.add(it)
                                    updateCanBeCooked()
                                    filterListAndUpdateRecyclerView()
                                }
                            }

                            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                                val product = snapshot.getValue(Product::class.java)
                                product?.let {
                                    val index = productList.indexOfFirst { p -> p.id == it.id }
                                    if (index >= 0) {
                                        productList[index] = it
                                        updateCanBeCooked()
                                        filterListAndUpdateRecyclerView()
                                    }
                                }
                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {
                                val product = snapshot.getValue(Product::class.java)
                                product?.let {
                                    productList.removeAll { p -> p.id == it.id }
                                    updateCanBeCooked()
                                    filterListAndUpdateRecyclerView()
                                }
                            }

                            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

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

    private fun updateRecyclerView() {
        adapter.notifyDataSetChanged()
    }

    private fun resetFilters() {
        filters["Primo"] = true
        filters["Secondo"] = true
        filters["Contorno"] = true
        filters["Dolce"] = true
    }

    private fun filterListAndUpdateRecyclerView(){
        Log.d("Filter", "Filtering list")
        filters.forEach { (key, value) ->
            Log.d("Filter", "$key: $value")
        }
        filteredRecipesList.clear()
        for(recipe in recipesList){
            if(filters[recipe.category] == true){
                filteredRecipesList.add(recipe)
            }
        }

        updateRecyclerView()
    }

    private fun updateCanBeCooked(){
        recipesList.forEach { recipe ->
            recipe.canBeCooked = recipe.canBeCooked(productList)
        }
    }



}

