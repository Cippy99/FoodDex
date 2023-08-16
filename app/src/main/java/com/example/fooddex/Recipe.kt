package com.example.fooddex

import androidx.annotation.DrawableRes

class Recipe() {
    var id = "0"
    var name: String = ""
    @DrawableRes
    var iconId = R.drawable.ic_grocery
    var category: String = "Primo"
    var nOfPeople: Int = 0
    var ingredients: MutableList<IngredientWithAmount> = mutableListOf()

    // TODO
    // - far funzionare bottone cucina


    constructor(id: String, name: String, category: String,
                nOfPeople: Int, ingredients: MutableList<IngredientWithAmount>, @DrawableRes iconId: Int) : this() {
        this.name = name
        this.id = id
        this.iconId = iconId
        this.category = category
        this.nOfPeople = nOfPeople
        this.ingredients = ingredients
    }
}