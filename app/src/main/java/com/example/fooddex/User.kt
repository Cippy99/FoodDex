package com.example.fooddex

class User (name: String?, email: String?, uid: String?) {

    var name: String? = null
    var email: String? = null
    var uid: String? = null
    

    init{
        this.name = name
        this.email = email
        this.uid = uid
    }
}