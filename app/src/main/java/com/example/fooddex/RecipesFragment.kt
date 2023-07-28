package com.example.fooddex

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.example.fooddex.databinding.FragmentRecipesBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [RecipesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecipesFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private var _binding: FragmentRecipesBinding?= null
    private var recipesList = mutableListOf<Recipe>()
    private lateinit var recyclerView: RecyclerView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentRecipesBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // vado a prendere le informazioni necessarie da Firebase
        // auth inizializzato come variabile privata
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid
        val dbref = Firebase.database.reference




        // da finire
    }


}