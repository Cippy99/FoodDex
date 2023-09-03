package com.example.fooddex

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

// Un adapter serve per collegare la view alla parte di dati del progetto.
// provvede quindi all'accesso ai dati da parte della view
class RecipeAdapter(val recipeList: MutableList<Recipe>, val context: Context): RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    inner class RecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        // questi campi li prende dalla view collegata
        private val recipeName: TextView = itemView.findViewById(R.id.recipeName)
        private val recipeImg: ImageView = itemView.findViewById(R.id.RecipeIcon)
        private val btnCucina: Button = itemView.findViewById(R.id.btnCucina)
        private lateinit var recipe: Recipe

        val auth = Firebase.auth
        val dbReference = Firebase.database.reference

        // lego i valori della view ai valori della classe Recipe

        fun bind(recipe: Recipe){
            this.recipe = recipe
            recipeName.text = recipe.name
            recipeImg.setImageResource(recipe.iconId)

            btnCucina.setOnClickListener {

                if(recipe.canBeCooked){
                    startActivityCookRecipe()
                }
                else{
                    var message = "Alcuni ingredienti necessari per cucinare la ricetta" +
                            "non sono presenti nel tuo inventario:\n"

                    for(product in recipe.missingIngredients){
                        message += "- ${product.name}\n"
                    }

                    message += "Cucinare comunque?"

                    MaterialAlertDialogBuilder(context)
                        .setTitle("Ingredienti insufficienti")
                        .setMessage(message)
                        .setPositiveButton("Cucina"){ _, _ ->
                            startActivityCookRecipe()}
                        .setNegativeButton("Cancella", null)
                        .show()
                }
            }
        }

        private fun startActivityCookRecipe(){
            val intent = Intent(context, CookRecipeActivity::class.java)
            intent.putExtra("recipeId", recipe.id)
            context.startActivity(intent)
        }
        init {
            val cardContainer: MaterialCardView = itemView.findViewById(R.id.cardContainer)

            cardContainer.setOnClickListener {
                editRecipe(adapterPosition)
            }
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
        val intent = Intent(context, EditRecipeActivity::class.java)
        intent.putExtra("recipeId", recipeList[position].id)
        context.startActivity(intent)
    }


}