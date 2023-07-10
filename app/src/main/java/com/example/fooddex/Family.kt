package com.example.fooddex


class Family() {
    var name: String = ""
    var creatorId: String = ""
    var id: String = ""
    var members: List<String> = listOf()

    constructor(name: String, code: String, creatorId: String, members: List<String>) : this() {
        this.name = name
        this.creatorId = creatorId
        this.id = code
        this.members = members
    }
}
