package com.example.fooddex

import com.google.firebase.database.Exclude
import java.time.LocalDate

class ExpirationDate(){
    var date: Long = 0
    var amount: Int = 0
    constructor(date: Long, amount: Int) : this() {
        this.date = date
        this.amount = amount
    }

    constructor(date: LocalDate, amount: Int = 0) : this(date.toEpochDay(), amount)

    @Exclude
    fun getDateInLocalDate(): LocalDate{
        return LocalDate.ofEpochDay(date)
    }

}