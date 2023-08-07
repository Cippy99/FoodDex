package com.example.fooddex
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter

class MealAdapter(var mealList: List<Meal>): RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    class MealViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val mealName: TextView = itemView.findViewById(R.id.mealName)
        private val mealIcon: ImageView = itemView.findViewById(R.id.MealIcon)
        private val mealTime : TextView = itemView.findViewById(R.id.mealTime)
        private val mealChef : TextView = itemView.findViewById(R.id.mealChef)

        fun bind(meal: Meal){
            mealName.text = meal.name
            val formatter : DateTimeFormatter= DateTimeFormatter.ofPattern("H:m")
            mealTime.text = meal.mealDatetime.format(formatter)
            mealChef.text = meal.chef!!.name
            mealIcon.setImageResource(meal.iconId)
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.meal_item, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(mealList[position])
    }

    override fun getItemCount(): Int {
        return mealList.size
    }

}