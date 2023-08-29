package com.example.fooddex

import com.google.firebase.database.Exclude

class Message() {
    var id: String = ""
    var senderId: String = ""
    var text: String = ""
    var timestamp: Long = 0
    @Exclude var senderName: String = ""

    constructor(id: String, senderId: String, text:String, timestamp: Long): this(){
        this.id = id
        this.senderId = senderId
        this.text = text
        this.timestamp = timestamp
    }
}