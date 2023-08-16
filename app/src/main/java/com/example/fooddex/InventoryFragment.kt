package com.example.fooddex

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddex.databinding.FragmentInventoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private lateinit var recyclerView: RecyclerView
    private val productList = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter

    private lateinit var dbReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        dbReference = Firebase.database.reference

        recyclerView = binding.rvProducts

        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.rv_spacing_8dp)!!)
        recyclerView.addItemDecoration(itemDecorator)


        adapter = ProductAdapter(productList, requireContext())
        recyclerView.adapter = adapter

        retrieveProductsFromDb()

        binding.fabAddProduct.setOnClickListener {
            val intent = Intent(activity, EditProductActivity::class.java)
            startActivity(intent)
        }

    }

    private fun updateRecyclerView() {
        adapter.notifyDataSetChanged()
    }

    private fun retrieveProductsFromDb() {
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
}