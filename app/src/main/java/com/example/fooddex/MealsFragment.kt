package com.example.fooddex

import android.os.Bundle
import android.util.Log
import com.example.fooddex.databinding.FragmentMealsBinding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.datepicker.MaterialDatePicker
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
import java.util.Calendar

class MealsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference
    private lateinit var selectedDate: LocalDate
    private var _binding: FragmentMealsBinding?= null
    private lateinit var adapter: MealAdapter

    private var mealList = mutableListOf<Meal>()
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMealsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        dbReference = Firebase.database.reference

        selectedDate = LocalDate.now()

        updateTextView()

        adapter = MealAdapter(mealList, requireContext())
        binding.recyclerView.adapter = adapter

        retrieveMealsFromDb()


        binding.btnDateBack.setOnClickListener{
            decreaseDate()
            mealList.clear() // Clear the list
            updateRecyclerView()
            retrieveMealsFromDb()
        }

        binding.btnDateForward.setOnClickListener {
            increaseDate()
            mealList.clear() // Clear the list
            updateRecyclerView()
            retrieveMealsFromDb()
        }

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Seleziona data")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener {selection->
            val selectedDateInMillis = selection ?: return@addOnPositiveButtonClickListener
            val c = Calendar.getInstance()
            c.timeInMillis = selectedDateInMillis
            selectedDate = LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH))
            updateTextView()
            mealList.clear() // Clear the list
            updateRecyclerView()
            retrieveMealsFromDb()
        }

        binding.tvDate.setOnClickListener{
            datePicker.show(parentFragmentManager, "tag")
        }
    }

    private fun updateTextView() {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        binding.tvDate.text = selectedDate.format(formatter)
    }

    private fun retrieveMealsFromDb() {
        val userId = auth.currentUser?.uid!!
        val userRef = dbReference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {

                        //Reference to meals of the selected date
                        val mealsRef: DatabaseReference = dbReference.child("meals")
                            .child(familyId).child(selectedDate.toEpochDay().toString())

                        // Add a ChildEventListener to fetch all products from the database under the familyId node
                        mealsRef.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                val meal = snapshot.getValue(Meal::class.java)

                                Log.d("debug", "Fetched Meal: $meal")

                                meal?.let {

                                    //Fetch Recipe
                                    val recipeRef = dbReference.child("recipes").child(familyId).child(meal.recipeId)

                                    recipeRef.addValueEventListener(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val recipe = snapshot.getValue(Recipe::class.java)
                                            Log.d("debug", "Fetched Recipe: $recipe")

                                            meal.recipe = recipe

                                            // Fetch Chef Name
                                            val chefRef = dbReference.child("users").child(meal.chefId).child("name")

                                            chefRef.addValueEventListener(object: ValueEventListener{
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val chefName = snapshot.value as String
                                                    Log.d("debug", "Fetched Chef: $chefName")

                                                    meal.chefName = chefName

                                                    mealList.add(it)
                                                    updateRecyclerView()

                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                }

                                            })

                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                        }

                                    })
                                }
                            }

                            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                                val meal = snapshot.getValue(Meal::class.java)
                                meal?.let {

                                    val recipeRef = dbReference.child("recipes").child(familyId).child(meal.recipeId)

                                    recipeRef.addValueEventListener(object : ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val recipe = snapshot.getValue(Recipe::class.java)
                                            Log.d("debug", "Fetched Recipe: $recipe")

                                            meal.recipe = recipe

                                            // Fetch Chef Name
                                            val chefRef = dbReference.child("users").child(meal.chefId).child("name")

                                            chefRef.addValueEventListener(object: ValueEventListener{
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    val chefName = snapshot.value as String
                                                    Log.d("debug", "Fetched Chef: $chefName")

                                                    meal.chefName = chefName

                                                    val index = mealList.indexOfFirst { m -> m.id == it.id }
                                                    if (index >= 0) {
                                                        mealList[index] = it
                                                        updateRecyclerView()
                                                    }

                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                }

                                            })

                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                        }

                                    })




                                }
                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {
                                val meal = snapshot.getValue(Meal::class.java)
                                meal?.let {
                                    mealList.removeAll { m -> m.id == it.id }
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

    private fun updateRecyclerView(){
        mealList.sortBy { it.time }
        adapter.notifyDataSetChanged()

    }

    private fun increaseDate(){
        selectedDate = selectedDate.plusDays(1)
        updateTextView()
    }

    private fun decreaseDate(){
        selectedDate = selectedDate.minusDays(1)
        updateTextView()
    }




}











