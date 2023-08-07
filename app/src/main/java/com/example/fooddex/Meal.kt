package com.example.fooddex

import androidx.annotation.DrawableRes
import java.time.LocalDateTime
import java.util.Date

class Meal() {
    var name: String = ""
    var recipe : Recipe?= null
    var iconId = R.drawable.ic_canned_food
    lateinit var mealDatetime: LocalDateTime
    var chef : User?= null
    constructor(name: String, recipe: Recipe, @DrawableRes iconId: Int, mealDateTime: LocalDateTime, chef: User) : this() {
        this.name = name
        this.recipe = recipe
        this.iconId = iconId
        this.mealDatetime = mealDatetime
        this.chef = chef
    }


}