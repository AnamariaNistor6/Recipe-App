package com.example.recipebook.data

import androidx.room.*
import com.example.recipebook.model.PendingOperation

@Dao
interface PendingOperationsDao {

    @Query("SELECT * FROM pending_operations ORDER BY timestamp ASC")
    suspend fun getAllPendingOperations(): List<PendingOperation>

    @Insert
    suspend fun insert(operation: PendingOperation)

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM pending_operations")
    suspend fun clearAll()

    @Query("DELETE FROM pending_operations WHERE recipeId = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Int)
}