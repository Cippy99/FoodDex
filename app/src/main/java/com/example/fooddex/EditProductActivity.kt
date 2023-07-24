package com.example.fooddex

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.AutoCompleteTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddex.databinding.ActivityEditProductBinding
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

class EditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProductBinding
    private lateinit var tietExpirationDate: TextInputEditText
    private lateinit var tietAmount: TextInputEditText
    private lateinit var recyclerView: RecyclerView
    private var selectedDate: LocalDate = LocalDate.now()
    private val allExpirationDates = mutableListOf<ExpirationDate>()

    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        dbReference = Firebase.database.reference

        recyclerView = binding.rvExpirations

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        binding.topAppBar.setOnMenuItemClickListener{menuItem ->
            when(menuItem.itemId){
                R.id.done ->{
                    val name = binding.tietName.text.toString()
                    val portion = binding.tietPortion.text.toString().toDouble()
                    val udm = (binding.tilPortionUM.editText as AutoCompleteTextView).text.toString()
                    Log.d("Debug", "Saving Product")
                    saveProduct(name, portion, udm, allExpirationDates)
                    true
                }
                else -> false
            }

        }

        //Inflate dialog view
        val dialogView = LayoutInflater.from(this@EditProductActivity).inflate(R.layout.add_expiration_dialog, null)
        tietExpirationDate = dialogView.findViewById(R.id.tietExpirationDate)
        tietAmount = dialogView.findViewById(R.id.tietAmount)

        //Create DatePicker
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Seleziona Data")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()
        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDateInMillis = selection ?: return@addOnPositiveButtonClickListener
            val c = Calendar.getInstance()
            c.timeInMillis = selectedDateInMillis

            datePicker.selection
            selectedDate = LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy")
            tietExpirationDate.setText(selectedDate.format(formatter))
        }

        //Open Datepicker when click on EditText
        tietExpirationDate.setOnClickListener {
            datePicker.show(supportFragmentManager, "tag")
        }

        //Create Dialog
        val dialog = MaterialAlertDialogBuilder(this@EditProductActivity)
            .setTitle("Aggiungi Scadenza")
            .setView(dialogView)
            .setPositiveButton("Conferma") { dialog, _ ->
                val amountText = tietAmount.text.toString()
                val amount = if (amountText.isNotEmpty()) amountText.toInt() else 0
                allExpirationDates.add(ExpirationDate(selectedDate, amount))
                tietExpirationDate.text = null
                tietAmount.text = null
                updateRecyclerView()
                dialog.dismiss()
            }
            .setNegativeButton("Cancella") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        //Open Dialog When click on AddExpiration
        binding.btnAddExpiration.setOnClickListener {
            dialog.show()
        }


    }

    private fun saveProduct(name: String, portion: Double, udm: String, allExpirationDates: MutableList<ExpirationDate>) {
        val userRef = dbReference.child("users").child(auth.currentUser?.uid!!)

        userRef.child("familyId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val familyId = snapshot.getValue(String::class.java)

                // FAMILY_CODE might be null if the "familyId" property doesn't exist in the database
                if (familyId != null) {
                    // Create a reference to the "products" node under the FAMILY_CODE
                    val productRef = dbReference.child("products").child(familyId).push()

                    // Get the unique ID generated by push() and set it in the product object
                    val productID = productRef.key

                    val product = Product(productID!!, name, portion, udm, 0, allExpirationDates)

                    // Save the product to the database using the productRef
                    productRef.setValue(product)

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
        val adapter = ExpirationAdapter(allExpirationDates)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

}