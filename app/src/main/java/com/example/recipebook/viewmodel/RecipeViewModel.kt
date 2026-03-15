package com.example.recipebook.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.recipebook.data.RecipeDatabase
import com.example.recipebook.data.RecipeRepository
import com.example.recipebook.model.Recipe
import com.example.recipebook.network.ApiClient
import com.example.recipebook.network.RecipeWebSocket
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "RecipeViewModel"
    }

    private val repository: RecipeRepository
    private val webSocket: RecipeWebSocket

    val recipes: LiveData<List<Recipe>>

    init {
        Log.d(TAG, "Initializing RecipeViewModel")
        val db = RecipeDatabase.getDatabase(application)

        repository = RecipeRepository(
            dao = db.recipeDao(),
            pendingDao = db.pendingOperationsDao(),
            api = ApiClient.api
        )

        recipes = repository.recipes

        // Initial server sync (happens once)
        viewModelScope.launch {
            Log.d(TAG, "Starting initial server sync")
            repository.refreshFromServer()

        }



        // WebSocket for real-time updates
        webSocket = RecipeWebSocket {
            Log.d(TAG, "WebSocket update received, refreshing data")
            viewModelScope.launch {
                repository.refreshFromServer()
            }
        }

        webSocket.connect()
    }

    fun getRecipeById(id: Int): LiveData<Recipe> {
        Log.d(TAG, "Getting recipe by ID: $id")
        return repository.getRecipeById(id)
    }

    fun insertRecipe(recipe: Recipe, onResult: (String?) -> Unit) {
        Log.d(TAG, "Insert recipe requested: ${recipe.title}")
        viewModelScope.launch {
            val result = repository.insert(recipe)
            result.onSuccess {
                Log.d(TAG, "Recipe inserted successfully")
                onResult(null)
            }.onFailure { error ->
                Log.e(TAG, "Recipe insert error: ${error.message}")
                onResult(error.message)
            }
        }
    }

    fun updateRecipe(recipe: Recipe, onResult: (String?) -> Unit) {
        Log.d(TAG, "Update recipe requested: ${recipe.title} (ID: ${recipe.recipeId})")
        viewModelScope.launch {
            val result = repository.update(recipe)
            result.onSuccess {
                Log.d(TAG, "Recipe updated successfully")
                onResult(null)
            }.onFailure { error ->
                Log.e(TAG, "Recipe update error: ${error.message}")
                onResult(error.message)
            }
        }
    }

    fun deleteRecipe(id: Int, onResult: (String?) -> Unit) {
        Log.d(TAG, "Delete recipe requested: ID $id")
        viewModelScope.launch {
            val result = repository.delete(id)
            result.onSuccess {
                Log.d(TAG, "Recipe deleted successfully")
                onResult(null)
            }.onFailure { error ->
                Log.e(TAG, "Recipe delete error: ${error.message}")
                onResult(error.message)
            }
        }
    }

    fun deletePendingRecipeById(recipeId: Int, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                repository.deletePendingRecipeByRecipeId(recipeId)
                Log.d("RecipeViewModel", "Pending recipe $recipeId deleted")
                onResult(null)
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Failed to delete pending recipe: ${e.message}")
                onResult(e.message)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }

}