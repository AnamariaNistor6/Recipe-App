package com.example.recipebook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rentals")
data class Rental(
    @PrimaryKey(autoGenerate = true)
    val rentalId: Int = 0,
    val date: String,
    val amount: Int,
    val type: String,
    val category: String,
    val description: String
)
