package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class CookRecipeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCookRecipeBinding
    private var selectedDateTime = LocalDateTime.now().withSecond(0)

    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference
    private val client = OkHttpClient()
    private val key = "AAAA2aoRqwk:APA91bFGtBQyYLp6CgBqZYyfSXJt4AR2nAs7_zQpz14lWykfvdadyw3yAWgFC5zxA-wnCbKBSSejSM1h07EYaEaOdAig5p1DZ32c8C-Rj5p3KmIKBp900Sanf53ggJCWcUQ11AdrH5Tz"

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
                    saveMealAndNotifyFamily()
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


    private fun saveMealAndNotifyFamily(){
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

    private fun notifyFamily() {
        Log.d("Notification", "Notifying Family")
        val title = "Nuovo pasto programmato"

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy")
        val body = if (recipe != null) "${recipe!!.name} il giorno ${selectedDateTime.format(formatter)} " +
                "alle ${selectedDateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))}" else ""

        val currentUser = auth.currentUser!!.uid
        val userRef = dbReference.child("users").child(currentUser)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {

                        val familyRef = dbReference.child("families").child(familyId)

                        familyRef.addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                Log.d("Notification", "Fetched family: $snapshot")
                                val family = snapshot.getValue(Family::class.java)

                                if (family != null){
                                    for (member in family.members){

                                        //Don't send Notification if to the user who made the meal
                                        if(member == currentUser){
                                            continue
                                            Log.d("Notification", "Skipping user $member")
                                        }

                                        val tokensRef = dbReference.child("fcm_tokens").child(member)

                                        tokensRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                for (tokenSnapshot in dataSnapshot.children) {
                                                    val token = tokenSnapshot.getValue(String::class.java)
                                                    if (token != null) {
                                                        sendNotificationToDevice(token, title, body)
                                                    }
                                                }
                                            }
                                            override fun onCancelled(databaseError: DatabaseError) {
                                                TODO("Not yet implemented")
                                            }
                                        })

                                }
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
                TODO("Not yet implemented")
            }

        })


    }

    private fun sendNotificationToDevice(token: String, title:String, body: String, data: Map<String, String> = emptyMap()){
        val url = "https://fcm.googleapis.com/fcm/send"

        val bodyJson = JSONObject()
        bodyJson.put("to", token)
        bodyJson.put("notification",
            JSONObject().also {
                it.put("title", title)
                it.put("body", body)
            }
        )
        bodyJson.put("data", JSONObject(data))

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "key=$key")
            .post(
                bodyJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCM API", e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("FCM API","Received data: ${response.body?.string()}")
            }

        })
    }

}