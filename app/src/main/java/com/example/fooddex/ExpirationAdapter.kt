package com.example.fooddex

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpirationAdapter(val expirationList: MutableList<ExpirationDate>): RecyclerView.Adapter<ExpirationAdapter.ExpirationViewHolder>() {

    inner class ExpirationViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val tvExpiration: TextView = itemView.findViewById(R.id.tvText)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(date: LocalDate, amount: Int){
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yy")
            tvExpiration.text = "${date.format(formatter)} - $amount"

            btnDelete.setOnClickListener {
                expirationList.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                notifyItemRangeChanged(adapterPosition, itemCount)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpirationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.text_removable_item, parent, false)

        return ExpirationViewHolder(view)
    }

    override fun getItemCount(): Int {
        return expirationList.size
    }

    override fun onBindViewHolder(holder: ExpirationViewHolder, position: Int) {
        holder.bind(expirationList[position].getDateInLocalDate(), expirationList[position].amount)
    }



}