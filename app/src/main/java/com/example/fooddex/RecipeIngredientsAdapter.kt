package com.example.fooddex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// classe che implementa l'adapter per la recycle view che mostra tutti gli ingredienti di una ricetta
class RecipeIngredientsAdapter(val ingredientsList: MutableList<Pair<Product,Double>>): RecyclerView.Adapter<RecipeIngredientsAdapter.IngredientsViewHolder>() {

    inner class IngredientsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val tvIngredient: TextView = itemView.findViewById(R.id.tvText)

        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        // lego gli elementi della view ai dati
        fun bind(pair: Pair<Product,Double>){
            tvIngredient.text = "${pair.first.name} - ${pair.second} ${pair.first.getShortUnitOfMeasure()}"

            btnDelete.setOnClickListener {
                ingredientsList.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                notifyItemRangeChanged(adapterPosition, itemCount)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.text_removable_item, parent, false)

        return IngredientsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ingredientsList.size
    }

    override fun onBindViewHolder(holder: IngredientsViewHolder, position: Int) {
        holder.bind(ingredientsList[position])
    }



}