package com.example.recipebook.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_operations")
data class PendingOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val operationType: OperationType,
    val recipeId: Int,
    val recipeData: String? = null, // JSON string of Recipe object
    val timestamp: Long = System.currentTimeMillis()
)

enum class OperationType {
    CREATE,
    UPDATE,
    DELETE
}