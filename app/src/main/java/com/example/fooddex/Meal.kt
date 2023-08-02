package com.example.fooddex

import java.time.LocalDateTime
import java.util.Date

class Meal() {
    var name: String = ""
    var recipe : Recipe?= null
    var imgRef: String = ""
    lateinit var mealDatetime: LocalDateTime
    var chef : User?= null
    constructor(name: String, recipe: Recipe, imgRef:String, mealDateTime: LocalDateTime, chef: User) : this() {
        this.name = name
        this.recipe = recipe
        this.imgRef = imgRef
        this.mealDatetime = mealDatetime
        this.chef = chef
    }


}