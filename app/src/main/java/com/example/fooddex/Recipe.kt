package com.example.fooddex

class Recipe() {
    var id = "0"
    var name: String = ""
    var imgRef: String = ""
    var category: String = ""
    var nOfPerson: Int = 0
    var products: MutableList<Product>? = null

    constructor(id: String, name: String, imgRef: String, category: String,
                nOfPerson: Int, products: MutableList<Product>) : this() {
        this.name = name
        this.id = id
        this.imgRef = imgRef
        this.category = category
        this.nOfPerson = nOfPerson
        this.products = products
    }
}