package com.example.fooddex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class IngredientSelectionDialogFragment: DialogFragment(), IngredientAmountDialogListener {

    private lateinit var rvProducts: RecyclerView
    private lateinit var adapter: SimpleProductAdapter
    private val productList = mutableListOf<Product>()

    private lateinit var callback: (product: Product, amount: Double) -> Unit

    fun onIngredientSelected(callback: (product: Product, amount: Double) -> Unit){
        this.callback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_choose_product, container, false)
        val topAppBar: MaterialToolbar = view.findViewById(R.id.topAppBar)
        topAppBar.setNavigationOnClickListener {
            dismiss()
        }

        rvProducts = view.findViewById(R.id.rvProducts)

        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.rv_spacing_8dp)!!)
        rvProducts.addItemDecoration(itemDecorator)

        adapter = SimpleProductAdapter(productList, this, requireContext())
        rvProducts.adapter = adapter

        retrieveProductsFromDb()

        return view
    }

    private fun retrieveProductsFromDb() {

        val auth = Firebase.auth
        val dbReference = Firebase.database.reference

        val userId = auth.currentUser?.uid!!
        val userRef = dbReference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {
                        val productsRef: DatabaseReference = dbReference.child("products").child(familyId)

                        // Add a ChildEventListener to fetch all products from the database under the familyId node
                        productsRef.addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                val product = snapshot.getValue(Product::class.java)
                                product?.let {
                                    productList.add(it)
                                    updateRecyclerView()
                                }
                            }

                            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                                val product = snapshot.getValue(Product::class.java)
                                product?.let {
                                    val index = productList.indexOfFirst { p -> p.id == it.id }
                                    if (index >= 0) {
                                        productList[index] = it
                                        updateRecyclerView()
                                    }
                                }
                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {
                                val product = snapshot.getValue(Product::class.java)
                                product?.let {
                                    productList.removeAll { p -> p.id == it.id }
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

    override fun onAmountSelected(product: Product, amount: Double) {
        callback(product, amount)
        dismiss()
    }
}