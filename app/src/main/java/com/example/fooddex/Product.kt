package com.example.fooddex

import androidx.annotation.DrawableRes

class Product() {
    var id = "0"
    var name: String = ""
    var portionSize: Double = 0.0
    var unitOfMeasure: String = ""
    @DrawableRes
    var imageReference: Int? = null
    var expirations: MutableList<ExpirationDate>? = null

    constructor(
        id: String, name: String, portionSize: Double, unitOfMeasure: String, @DrawableRes imageReference: Int,
        expirations: MutableList<ExpirationDate>) : this() {
        this.id = id
        this.name = name
        this.portionSize = portionSize
        this.unitOfMeasure = unitOfMeasure
        this.imageReference = imageReference
        this.expirations = expirations
    }
}