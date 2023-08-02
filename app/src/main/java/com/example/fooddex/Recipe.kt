package com.example.fooddex

class Recipe() {
    var id = "0"
    var name: String = ""
    var imgRef: String = ""
    var category: String = ""
    var nOfPerson: Int = 0
    var ingredients: MutableList<Product>? = null
    // associare quantità -> lista di coppie (pair)

    constructor(id: String, name: String, category: String,
                nOfPerson: Int, ingredients: MutableList<Product>) : this() {
        this.name = name
        this.id = id
        //this.imgRef = imgRef
        this.category = category
        this.nOfPerson = nOfPerson
        this.ingredients = ingredients
    }
}