package com.example.fooddex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class EditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProductBinding
    private lateinit var recyclerView: RecyclerView
    private var allExpirationDates = mutableListOf<ExpirationDate>()
    private var productId: String? = null
    @DrawableRes private var iconId: Int = IconData.iconList[0].iconId


    private lateinit var auth: FirebaseAuth
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        dbReference = Firebase.database.reference

        binding.ivIcon.setImageResource(iconId)
        recyclerView = binding.rvExpirations

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        binding.topAppBar.setOnMenuItemClickListener{menuItem ->
            when(menuItem.itemId){
                R.id.done ->{
                    if(validate()){
                        val name = binding.tietName.text.toString()
                        val portion = binding.tietPortion.text.toString().toDouble()
                        val udm = (binding.tilPortionUM.editText as AutoCompleteTextView).text.toString()
                        saveProduct(name, portion, udm, allExpirationDates, iconId)
                    }

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

        //Open Dialog When click on AddExpiration
        binding.btnAddExpiration.setOnClickListener {
            val dialog = ExpirationDialog(this){expiration ->
                allExpirationDates.add(expiration)
                updateRecyclerView()
            }
            dialog.show()
        }

        productId = intent.getStringExtra("productId")

        if(!productId.isNullOrEmpty()){
            retrieveProductAndFillFields(productId!!)
        }
    }

    private fun validate(): Boolean {
        //Clear errors
        binding.tilName.error = null
        binding.tilPortion.error = null

        val name = binding.tietName.text.toString()
        val portion = binding.tietPortion.text.toString()
        var ok = true
        if (name.isNullOrEmpty()){
            ok = false
            binding.tilName.error = getString(R.string.empty_name)
        }

        if (portion.isNullOrEmpty()){
            ok = false
            binding.tilPortion.error = getString(R.string.empty_portion)
        }

        return ok
    }

    private fun retrieveProductAndFillFields(productId: String){
        val userId = auth.currentUser?.uid!!
        val userRef = dbReference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {
                        val productRef: DatabaseReference = dbReference.child("products").child(familyId).child(productId)

                        // Add a ChildEventListener to fetch all products from the database under the familyId node
                        productRef.addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val product = snapshot.getValue(Product::class.java)
                                if (product != null){
                                    fillFields(product)
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

    private fun fillFields(product: Product) {
        binding.tietName.setText(product.name)
        binding.tietPortion.setText(product.portionSize.toString())
        (binding.tilPortionUM.editText as AutoCompleteTextView).setText(product.unitOfMeasure, false)
        allExpirationDates = product.expirations
        binding.ivIcon.setImageResource(product.iconId)
        iconId = product.iconId
        updateRecyclerView()
    }

    private fun saveProduct(name: String, portion: Double, udm: String, allExpirationDates: MutableList<ExpirationDate>, iconId: Int) {
        val userRef = dbReference.child("users").child(auth.currentUser?.uid!!)

        userRef.child("familyId").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val familyId = snapshot.getValue(String::class.java)

                // FAMILY_CODE might be null if the "familyId" property doesn't exist in the database
                if (familyId != null) {
                    // Create a reference to the "products" node under the FAMILY_CODE
                    if(productId.isNullOrEmpty()){

                        val productRef = dbReference.child("products").child(familyId).push()

                        // Get the unique ID generated by push() and set it in the product object
                        val productID = productRef.key

                        val product = Product(productID!!, name, portion, udm, allExpirationDates, iconId)

                        // Save the product to the database using the productRef
                        productRef.setValue(product)
                    }
                    else{

                        val productRef = dbReference.child("products").child(familyId).child(productId!!)
                        val product = Product(productId!!, name, portion, udm, allExpirationDates, iconId)
                        productRef.setValue(product)
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
        allExpirationDates.sortBy { it.date }
        val adapter = ExpirationAdapter(allExpirationDates)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

}