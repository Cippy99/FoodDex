package com.example.fooddex

import androidx.annotation.DrawableRes
import com.google.firebase.database.Exclude
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class Product() {
    var id = "0"
    var name: String = ""
    var portionSize: Double = 0.0
    var unitOfMeasure: String = ""
    var expirations: MutableList<ExpirationDate> = mutableListOf()
    @DrawableRes
    var iconId = R.drawable.ic_grocery

    constructor(
        id: String, name: String, portionSize: Double, unitOfMeasure: String,
        expirations: MutableList<ExpirationDate>, @DrawableRes iconId: Int) : this() {
        this.id = id
        this.name = name
        this.portionSize = portionSize
        this.unitOfMeasure = unitOfMeasure
        this.expirations = expirations
        this.iconId = iconId
    }

    @Exclude
    fun getTotalSize(): Double{
        return getTotalAmount() * portionSize
    }

    @Exclude
    fun getTotalAmount(): Int{
        return expirations.sumOf { it.amount }
    }



    @Exclude
    fun getNearestExpirationsInDays(): Long{
        if (expirations.size == 0){
            return 0
        }
        return expirations.minOf { it.date }
    }

    @Exclude
    fun getDaysUntilExpiration(): Int {
        val expirationDate = LocalDate.ofEpochDay(getNearestExpirationsInDays())
        val today = LocalDate.now()
        return ChronoUnit.DAYS.between(today, expirationDate).toInt()
    }

    @Exclude
    fun removeNearestExpirationItem() {
        val nearestExpiration = expirations.minByOrNull { it.date }

        nearestExpiration?.let {
            it.amount--
            if (it.amount <= 0) {
                expirations.remove(it)
            }
        }
    }

    @Exclude
    fun getShortUnitOfMeasure(): String{
        return when(this.unitOfMeasure.lowercase()){
            "grammi" -> "g"
            "litri" -> "l"
            "pezzi" -> "pz."
            else -> ""
        }
    }



}