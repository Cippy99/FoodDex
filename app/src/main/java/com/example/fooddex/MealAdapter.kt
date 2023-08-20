package com.example.fooddex
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.time.format.DateTimeFormatter

class MealAdapter(var mealList: List<Meal>, var context: Context): RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    inner class MealViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        private val mealName: TextView = itemView.findViewById(R.id.mealName)
        private val mealIcon: ImageView = itemView.findViewById(R.id.MealIcon)
        private val mealTime : TextView = itemView.findViewById(R.id.mealTime)
        private val mealChef : TextView = itemView.findViewById(R.id.mealChef)
        private val btnLike: MaterialButton = itemView.findViewById(R.id.btnLike)
        private var isLikedByCurrentUser = false
        val auth = Firebase.auth

        fun bind(meal: Meal){
            val formatter : DateTimeFormatter= DateTimeFormatter.ofPattern("H:m")
            mealTime.text = meal.getDateInLocalDateTime().format(formatter)

            if (meal.recipe != null){
                mealName.text = meal.recipe!!.name
                mealIcon.setImageResource(meal.recipe!!.iconId)
            }

            Log.d("debug", "Updating meal chef to: ${meal.chefName}")
            mealChef.text = meal.chefName

            val userId = auth.currentUser!!.uid

            isLikedByCurrentUser = meal.isLikedBy(userId)
            btnLike.text = meal.getNumberOfLikes().toString()

            btnLike.setOnClickListener {

                //Add or remove user from the list
                if(meal.isLikedBy(userId)){
                    meal.likedBy.remove(userId)
                }
                else {
                    meal.likedBy.add(userId)
                }
                isLikedByCurrentUser = meal.isLikedBy(userId)

                updateLikes(adapterPosition)
                notifyDataSetChanged()
                updateLikeButton()
            }

        }

        private fun updateLikeButton(){
            if(isLikedByCurrentUser){
                btnLike.setIconResource(R.drawable.baseline_thumb_up_alt_24)
            }
            else{
                btnLike.setIconResource(R.drawable.baseline_thumb_up_off_alt_24)
            }
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

    private fun updateLikes(position: Int){
        val auth = Firebase.auth
        val dbReference = Firebase.database.reference

        val userId = auth.currentUser?.uid!!
        val userRef = dbReference.child("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("familyId")) {
                    val familyId = snapshot.child("familyId").value as String
                    if (familyId.isNotEmpty()) {

                        val mealId = mealList[position].id
                        val mealDate = mealList[position].date.toString()

                        val likesRef: DatabaseReference = dbReference.child("meals").child(familyId).child(mealDate).child(mealId).child("likedBy")

                        likesRef.setValue(mealList[position].likedBy)


                    } else {
                        // Handle the case where familyId is empty or not available
                    }
                } else {
                    // Handle the case where familyId is not available in the database
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error if needed
            }
        })
    }

}