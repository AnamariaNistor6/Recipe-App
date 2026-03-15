package com.example.recipebook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val recipeId: Int = 0,
    val title: String,
    val ingredients: String,
    val instructions: String,
    val prepTimeMinutes: Int,
    val servings: Int
)
