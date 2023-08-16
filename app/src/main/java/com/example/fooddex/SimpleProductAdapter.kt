package com.example.fooddex

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class SimpleProductAdapter(private val productList: MutableList<Product>, private val listener: IngredientAmountDialogListener, val context: Context): RecyclerView.Adapter<SimpleProductAdapter.SimpleProductViewHolder>() {

    inner class SimpleProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivProductIcon)

        private lateinit var product: Product


        fun bind(product: Product){

            this.product = product

            tvName.text = product.name
            ivIcon.setImageResource(product.iconId)

        }

        init {

            val cardContainer: MaterialCardView = itemView.findViewById(R.id.cardContainer)

            cardContainer.setOnClickListener {
                //Open Dialog to input amount
                    val dialog = IngredientAmountDialog(context, product, listener)
                    dialog.show()


            }

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.simple_product_card_item, parent, false)

        return SimpleProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleProductViewHolder, position: Int) {
        holder.bind(productList[position])
    }

    override fun getItemCount(): Int {
        return productList.size
    }

}