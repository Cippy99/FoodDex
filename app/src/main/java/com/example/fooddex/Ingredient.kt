package com.example.fooddex

class Ingredient() {
    var name: String = ""
    var creatorId: String = ""
    var id: String = ""


    constructor(name: String, code: String, creatorId: String, members: List<String>) : this() {
        this.name = name
        this.creatorId = creatorId
        this.id = code

    }

}