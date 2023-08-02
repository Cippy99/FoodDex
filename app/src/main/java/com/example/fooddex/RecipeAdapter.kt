package com.example.fooddex

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.math.abs

// Un adapter serve per collegare la view alla parte di dati del progetto.
// provvede quindi all'accesso ai dati da parte della view
class RecipeAdapter(val recipeList: MutableList<Recipe>, val context: Context): RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        // questi campi li prende dalla view collegata
        private val recipeName: TextView = itemView.findViewById(R.id.recipeName)
        //private val recipeCategory: TextView = itemView.findViewById(R.id.recipeCategory)
        private val recipeImg: ImageView = itemView.findViewById(R.id.RecipeIcon)
        //private val recipeNOfPerson: TextView = itemView.findViewById(R.id.recipeNOfPerson)
        private val btnCucina: Button = itemView.findViewById(R.id.btnCucina)

        private lateinit var selectedDate: LocalDate
        private lateinit var product: Product

        val auth = Firebase.auth
        val dbReference = Firebase.database.reference

        // lego i valori della view ai valori della classe Recipe
        fun bind(recipe: Recipe){
            recipeName.text = recipe.name
            //recipeCategory.text = recipe.category
            recipeImg.setImageResource(recipe.imgRef.toInt())
            //recipeNOfPerson.text = recipe.nOfPerson.toString()
        }
        init {
            val cardContainer: MaterialCardView = itemView.findViewById(R.id.cardContainer)

            cardContainer.setOnClickListener {
                editRecipe(adapterPosition)
            }
        }
        private fun updateProduct(){
            val userRef = dbReference.child("users").child(auth.currentUser?.uid!!)

            userRef.child("familyId").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val familyId = snapshot.getValue(String::class.java)

                    if (familyId != null) {

                        val recipeRef = dbReference.child("recipes").child(familyId)
                        recipeRef.child(product.id).setValue(product)
                        notifyItemChanged(adapterPosition)

                    } else {
                        // Handle the case when "familyId" doesn't exist in the database for the user
                        // You can show an error message or take appropriate action
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)

        return RecipeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipeList[position])
    }

    // funzione che consente di modificare il prodotto quando schiaccio.
    // da modificare per adattarla a ricetta
    fun editRecipe(position: Int){
        val intent = Intent(context, EditProductActivity::class.java)
        intent.putExtra("recipeId", recipeList[position].id)
        context.startActivity(intent)
    }


}