package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AutoCompleteTextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddex.databinding.ActivityEditProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

public class AddMealActivity extends AppCompatActivity {
    private lateinit var binding: ActivityEditProductBinding
    private lateinit var recyclerView: RecyclerView
    private var allChef = mutableListOf<User>()
    private var mealId: String? = null
    @DrawableRes private var iconId: Int = IconData.iconList[0].iconId


    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference


}
