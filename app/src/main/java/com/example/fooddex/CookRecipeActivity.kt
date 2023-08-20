package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import com.example.fooddex.databinding.ActivityCookRecipeBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class CookRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCookRecipeBinding
    private var selectedDateTime = LocalDateTime.now().withSecond(0)

    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference

    private var recipe: Recipe? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCookRecipeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        dbReference = Firebase.database.reference

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        //Set date and time to current Date and Time
        updateDate()
        updateTime()

        retrieveRecipeAndFillFields(intent.getStringExtra("recipeId")!!)

        //Build Date Picker
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Seleziona data")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener {selection->
            val selectedDateInMillis = selection ?: return@addOnPositiveButtonClickListener
            val c = Calendar.getInstance()
            c.timeInMillis = selectedDateInMillis

            selectedDateTime = selectedDateTime
                .withYear(c.get(Calendar.YEAR))
                .withMonth(c.get(Calendar.MONTH)+1)
                .withDayOfMonth(c.get(Calendar.DAY_OF_MONTH))

            updateDate()
        }

        //Open Date Picker
        binding.tietDate.setOnClickListener {
            datePicker.show(supportFragmentManager, "tag")
        }

        //Build Time Picker
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setTitleText("Seleziona Ora")
            .setInputMode(INPUT_MODE_CLOCK)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            selectedDateTime = selectedDateTime.withHour(timePicker.hour).withMinute(timePicker.minute)
            updateTime()
        }


        //Show Time Picker
        binding.tietTime.setOnClickListener {
            timePicker.show(supportFragmentManager, "tag")
        }

        binding.topAppBar.setOnMenuItemClickListener{menuItem ->
            Log.d("debug", "Clicked checkmark")
            when(menuItem.itemId){
                R.id.done ->{
                    saveMeal()
                    finish()
                    true
                }
                else -> false
            }

        }

    }

    private fun updateTime() {
        binding.tietTime.setText(selectedDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
    }

    private fun updateDate(){
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy")
        binding.tietDate.setText(selectedDateTime.format(formatter))
    }


    private fun retrieveRecipeAndFillFields(recipeId: String){
        val userId = auth.currentUser?.uid!!
        val userRef = dbReference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {
                        val recipeRef: DatabaseReference = dbReference.child("recipes").child(familyId).child(recipeId)

                        // Fetch the recipe
                        recipeRef.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //Save the recipe
                                recipe = snapshot.getValue(Recipe::class.java)
                                if (recipe != null){
                                    fillFields()
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

    private fun fillFields(){
        if (recipe != null){
            binding.ivIcon.setImageResource(recipe!!.iconId)
            binding.tvName.text = recipe!!.name
        }

    }


    private fun saveMeal(){
        if(recipe != null){
            val userId = auth.currentUser?.uid!!
            val userRef = dbReference.child("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChild("familyId")) {
                        val familyId = snapshot.child("familyId").value as String
                        if (familyId.isNotEmpty()) {

                            val mealRef: DatabaseReference = dbReference.child("meals")
                                .child(familyId).child(selectedDateTime.toLocalDate().toEpochDay().toString())

                            //Save Meal
                            val mealKey = mealRef.push().key

                            val meal = Meal(mealKey!!, recipe!!.id, selectedDateTime, userId)

                            if (mealKey != null) {
                                // Save Meal under the generated key
                                mealRef.child(mealKey).setValue(meal)
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
}