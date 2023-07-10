package com.example.fooddex

class User (name: String, email: String, uid: String) {

    var name: String
    var email: String
    var uid: String
    var familyId: String? = null


    init{
        this.name = name
        this.email = email
        this.uid = uid
    }
}