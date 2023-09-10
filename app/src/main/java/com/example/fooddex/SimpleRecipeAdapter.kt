package com.example.fooddex

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SimpleRecipeAdapter(val recipeList: MutableList<Recipe>, val context: Context, var selectedRecipeId: String): RecyclerView.Adapter<SimpleRecipeAdapter.RecipeViewHolder>() {

    var selectedRecipe: Recipe? = null

    inner class RecipeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val recipeName: TextView = itemView.findViewById(R.id.tvName)
        private val recipeImg: ImageView = itemView.findViewById(R.id.ivItemIcon)
        private val cardContainer: MaterialCardView = itemView.findViewById(R.id.cardContainer)
        private lateinit var recipe: Recipe

        val auth = Firebase.auth
        val dbReference = Firebase.database.reference

        fun bind(recipe: Recipe){
            this.recipe = recipe
            recipeName.text = recipe.name
            recipeImg.setImageResource(recipe.iconId)

            cardContainer.isCheckable = true

            if(selectedRecipeId == recipe.id){
                selectedRecipe = recipe
            }

            val isSelected = selectedRecipeId == recipe.id
            cardContainer.isChecked = isSelected

            cardContainer.setOnClickListener {
                selectedRecipe = recipe
                selectedRecipeId = recipe.id
                Log.d("Debug", "Tap on recipe ${recipe.name} with id ${recipe.id}")
                notifyDataSetChanged()
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.simple_item_card_item, parent, false)

        return RecipeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(recipeList[position])
    }


}