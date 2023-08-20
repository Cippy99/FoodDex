package com.example.fooddex

import androidx.annotation.DrawableRes
import com.google.firebase.database.Exclude
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.Date

class Meal() {
    var id: String = ""
    var recipeId : String = ""
    var date:Long = 0
    var time: Int = 0
    var chefId : String = ""
    var likedBy = mutableListOf<String>()

    //Not Saved in Db
    @Exclude
    var chefName: String? = ""
    @Exclude
    var recipe: Recipe? = null


    constructor(id: String, recipeId: String, mealDateTime: LocalDateTime, chefId: String) : this() {
        this.id = id
        this.recipeId = recipeId

        this.date = mealDateTime.toLocalDate().toEpochDay()
        this.time = mealDateTime.toLocalTime().toSecondOfDay()
        this.chefId = chefId
    }

    @Exclude
    fun getDateInLocalDateTime(): LocalDateTime{
        return LocalDateTime.of(LocalDate.ofEpochDay(date), LocalTime.ofSecondOfDay(time.toLong()))
    }

    @Exclude
    fun getNumberOfLikes(): Int{
        return likedBy.size
    }
    @Exclude
    fun isLikedBy(userId: String): Boolean{
        return likedBy.contains(userId)
    }


}