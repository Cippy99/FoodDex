package com.example.fooddex

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.fooddex.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        dbRef = Firebase.database.reference

        retireveAndSetUsername()

        binding.btnLogout.setOnClickListener {
            val intent = Intent(activity, Login::class.java)
            auth.signOut()
            startActivity(intent)
            //Maybe call back activity and let it decide if t should close itself (maybe displaying a dialog)
            activity?.finish()
        }

        binding.btnFamily.setOnClickListener {
            //Start Family Setting Activity

            val userId = auth.currentUser?.uid!!
            var familyId: String? = null

            dbRef.child("users").child(userId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChild("familyId")) {
                        familyId = snapshot.child("familyId").value as String
                    }

                    val intent = if(!familyId.isNullOrEmpty()){
                        Intent(activity, FamilySettingsActivity::class.java)
                    } else{
                        Intent(activity, NoFamilySettingActivity::class.java)
                    }

                    startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {
                    //TODO
                }
            })


        }
    }

    private fun retireveAndSetUsername() {
        val userRef = dbRef.child("users").child(auth.currentUser!!.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java)

                if(name != null){
                    setUsername(name)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun setUsername(username: String){
        binding.tvUserName.text = username

        val userInitial = requireActivity().findViewById<TextView>(R.id.user_initial)
        userInitial.text = username[0].uppercase()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}