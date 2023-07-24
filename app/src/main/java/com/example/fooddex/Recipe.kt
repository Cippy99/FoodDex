package com.example.fooddex

import java.util.Date

class Recipe() {

    var name: String = ""
    var id: String = ""
    var creatorId: String = ""
    var ingredients: List<Ingredient> = listOf()
    var creationDate: Date?= null

    constructor(name: String, code: String, creatorId: String, members: List<String>) : this() {
        this.name = name
        this.creatorId = creatorId
        this.id = code
        this.ingredients =ingredients
        this.creationDate = creationDate

    }
}