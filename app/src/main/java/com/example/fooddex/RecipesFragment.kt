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
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private lateinit var dbref: DatabaseReference
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
        val userId = auth.currentUser!!.uid
        val dbref = Firebase.database.reference

        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.rv_spacing_8dp)!!)
        recyclerView.addItemDecoration(itemDecorator)


        adapter = RecipeAdapter(recipesList, requireContext())
        recyclerView.adapter = adapter

        retrieveRecipesFromDb()

        binding.fabAddRecipe.setOnClickListener {
            val intent = Intent(activity, EditRecipeActivity::class.java)
            startActivity(intent)
        }

    }

    // importa le ricette dal db
    // funziona tramite gli event listener che notificano quando c'è un cambiamento
    private fun retrieveRecipesFromDb() {
        val userId = auth.currentUser?.uid!!
        val userRef = dbref.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {
                        val recipesRef: DatabaseReference = dbref.child("recipes").child(familyId)

                        // Add a ChildEventListener to fetch all products from the database under the familyId node
                        recipesRef.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                Log.d("debug", "$snapshot")
                                val recipe = snapshot.getValue(Recipe::class.java)
                                Log.d("debug", "$recipe")
                                recipe?.let {
                                    recipesList.add(it)
                                    updateRecyclerView()
                                    Log.d("Debug", "List has now ${recipesList.size} elements")
                                }
                            }

                            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                                val recipe = snapshot.getValue(Recipe::class.java)
                                recipe?.let {
                                    val index = recipesList.indexOfFirst { p -> p.id == it.id }
                                    if (index >= 0) {
                                        recipesList[index] = it
                                        updateRecyclerView()
                                    }
                                }
                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {
                                val recipe = snapshot.getValue(Recipe::class.java)
                                recipe?.let {
                                    recipesList.removeAll { p -> p.id == it.id }
                                    updateRecyclerView()
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
                // Handle database error if needed
            }
        })
    }

    private fun updateRecyclerView() {
        adapter.notifyDataSetChanged()
    }



}