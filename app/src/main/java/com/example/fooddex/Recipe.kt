package com.example.fooddex

import androidx.annotation.DrawableRes
import com.google.firebase.database.Exclude

class Recipe() {
    var id = "0"
    var name: String = ""
    @DrawableRes
    var iconId = R.drawable.ic_grocery
    var category: String = "Primo"
    var nOfPeople: Int = 0
    var ingredients: MutableList<IngredientWithAmount> = mutableListOf()
    @Exclude
    var canBeCooked: Boolean = true
    @Exclude
    var missingIngredients: List<Product> = listOf()

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

    fun canBeCooked(inventory: List<Product>): Boolean {
        for (ingredient in ingredients) {
            // Find the product in the inventory that matches the ingredient's productId
            val matchingProduct = inventory.find { product -> product.id == ingredient.productId }

            if (matchingProduct == null || matchingProduct.getTotalSize() < ingredient.amount) {
                return false
            }
        }

        return true
    }

    fun missingIngredients(inventory: List<Product>): List<Product>{
        var missingIngredients = mutableListOf<Product>()
        for (ingredient in ingredients) {
            // Find the product in the inventory that matches the ingredient's productId
            val matchingProduct = inventory.find { product -> product.id == ingredient.productId }

            if (matchingProduct != null && matchingProduct.getTotalSize() < ingredient.amount) {
                missingIngredients.add(matchingProduct)
            }
        }
        return missingIngredients
    }

}