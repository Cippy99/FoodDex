package com.example.fooddex

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.fooddex.databinding.FragmentMealsBinding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MealsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var selectedDate: LocalDate
    private var _binding: FragmentMealsBinding?= null

    private var mealList = mutableListOf<Meal>()
    private val binding get() = _binding!!
    private lateinit var familyCode : String



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

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid // Ottiene l'UID dell'utente correntemente autenticato
        val dbRef = Firebase.database.reference
        selectedDate = LocalDate.now()

        updateTextView()

        binding.btnDateBack.setOnClickListener{
            decreaseDate()
            retrieveMealsFromDatabase(selectedDate, familyCode)
        }

        binding.btnDateForward.setOnClickListener {
            increaseDate()
            retrieveMealsFromDatabase(selectedDate, familyCode)
        }



        // Aggiunge un listener per un singolo valore sull'attributo "familyId" del nodo "users" relativo all'UID dell'utente corrente
        dbRef.child("users").child(userId).child("familyId").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    familyCode = snapshot.value as String // Ottiene il codice famiglia (family code) come stringa (es. FKUVLT)

                    retrieveMealsFromDatabase(selectedDate, familyCode)

                } else {
                    // Nessun nodo "familyId" trovato per l'utente corrente o il valore non esiste
                    // Puoi gestire questo caso a seconda delle tue esigenze
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Gestisce eventuali errori verificatisi durante la lettura del valore da Firebase
            }
        })

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
            retrieveMealsFromDatabase(selectedDate, familyCode)
        }

        binding.tvDate.setOnClickListener{
            datePicker.show(parentFragmentManager, "tag")
        }
    }

    private fun updateTextView() {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        binding.tvDate.setText(selectedDate.format(formatter))
    }

    // Dichiarazione della funzione retrieveMealsFromDatabase con i parametri date e familyCode
    private fun retrieveMealsFromDatabase(date: LocalDate, familyCode: String) {
        val dbRef = Firebase.database.reference
        val mealsRef = dbRef.child("meals").child(familyCode).child(date.toEpochDay().toString())

        mealsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(mealsSnapshot: DataSnapshot) {
                if (mealsSnapshot.exists()) {
                    mealList = mutableListOf<Meal>()

                    for (mealSnapshot in mealsSnapshot.children) {
                        val meal = mealSnapshot.getValue(Meal::class.java)
                        meal?.let { mealList.add(it) }
                    }

                    updateRecyclerView()

                } else {
                    // Nessun pasto trovato per la data e il codice famiglia specificati
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Gestisce eventuali errori verificatisi durante la lettura dei dati da Firebase
            }
        })
    }

    private fun updateRecyclerView(){
        val adapter = MealAdapter(mealList)
        binding.recyclerView.adapter = adapter
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











