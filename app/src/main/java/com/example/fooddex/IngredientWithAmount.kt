package com.example.fooddex

class IngredientWithAmount() {
    var productId: String = ""
    var amount: Double = 0.0

    constructor(productId: String, amount: Double): this(){
        this.productId = productId
        this.amount = amount
    }

    constructor(product: Product, amount: Double): this(){
        this.productId = product.id
        this.amount = amount
    }
}