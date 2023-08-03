package com.example.fooddex

class Recipe() {
    var id = "0"
    var name: String = ""
    var imgRef: String = ""
    var category: String = ""
    var nOfPerson: Int = 0
    var ingredients: MutableList<Pair<Product,Int>>? = null
    // uso pair per associare una quantità ad ogni ngrediente all'interno della ricetta.

    constructor(id: String, name: String, category: String,
                nOfPerson: Int, ingredients: MutableList<Pair<Product,Int>>) : this() {
        this.name = name
        this.id = id
        //this.imgRef = imgRef
        this.category = category
        this.nOfPerson = nOfPerson
        this.ingredients = ingredients
    }
}