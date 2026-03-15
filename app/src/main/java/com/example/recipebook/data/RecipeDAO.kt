package com.example.recipebook.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.recipebook.model.Recipe

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun getAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE recipeId = :id")
    fun getRecipeById(id: Int): LiveData<Recipe>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<Recipe>)

    @Update
    suspend fun update(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE recipeId = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM recipes")
    suspend fun clearAll()
}
