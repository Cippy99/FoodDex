package com.example.fooddex

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fooddex.databinding.FragmentInventoryBinding
import com.google.firebase.auth.FirebaseAuth

class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
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

        binding.fabAddProduct.setOnClickListener {
            val intent = Intent(activity, EditProductActivity::class.java)
            startActivity(intent)
        }

    }
}