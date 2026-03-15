package com.example.recipebook.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.recipebook.model.OperationType
import com.example.recipebook.model.PendingOperation
import com.example.recipebook.model.Recipe
import com.example.recipebook.network.RecipeApi
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository(
    private val dao: RecipeDao,
    private val pendingDao: PendingOperationsDao,
    private val api: RecipeApi
) {
    companion object {
        private const val TAG = "RecipeRepository"
    }

    private val gson = Gson()

    val recipes: LiveData<List<Recipe>> = dao.getAllRecipes()

    fun getRecipeById(id: Int): LiveData<Recipe> = dao.getRecipeById(id)

    // READ - Fetch from server once and cache
    suspend fun refreshFromServer(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting server sync...")

            // First, try to sync any pending operations
            syncPendingOperations()

            // Then fetch fresh data from server
            val serverRecipes = api.getRecipes()
            Log.d(TAG, "Received ${serverRecipes.size} recipes from server")

            dao.clearAll()
            dao.insertAll(serverRecipes)

            Log.d(TAG, "Successfully synced ${serverRecipes.size} recipes from server")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync from server: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Sync all pending operations to server
    private suspend fun syncPendingOperations() {
        try {
            val pendingOps = pendingDao.getAllPendingOperations()

            if (pendingOps.isEmpty()) {
                Log.d(TAG, "No pending operations to sync")
                return
            }

            Log.d(TAG, "Syncing ${pendingOps.size} pending operations...")

            for (operation in pendingOps) {
                try {
                    when (operation.operationType) {
                        OperationType.CREATE -> {
                            val recipe = gson.fromJson(operation.recipeData, Recipe::class.java)
                            val serverRecipe = api.createRecipe(recipe.copy(recipeId = 0))

                            // Replace temp recipe with server version
                            dao.deleteById(operation.recipeId)
                            dao.insert(serverRecipe)

                            Log.d(TAG, "Synced CREATE for: ${recipe.title}, server ID: ${serverRecipe.recipeId}")
                        }
                        OperationType.UPDATE -> {
                            val recipe = gson.fromJson(operation.recipeData, Recipe::class.java)
                            api.updateRecipe(recipe.recipeId, recipe)
                            Log.d(TAG, "Synced UPDATE for ID: ${recipe.recipeId}")
                        }
                        OperationType.DELETE -> {
                            // Try to delete from server, but don't fail if it doesn't exist
                            try {
                                api.deleteRecipe(operation.recipeId)
                                Log.d(TAG, "Synced DELETE for ID: ${operation.recipeId}")
                            } catch (e: Exception) {
                                // Recipe might already be deleted or not exist on server
                                Log.w(TAG, "DELETE sync failed for ID: ${operation.recipeId} - ${e.message}")
                            }
                        }
                    }

                    // Remove successfully synced operation
                    pendingDao.deleteById(operation.id)
                    Log.d(TAG, "Removed pending operation ${operation.id} from queue")

                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync operation ${operation.id}: ${e.message}")
                    // Keep operation in queue for next sync attempt
                }
            }

            Log.d(TAG, "Pending operations sync completed")

        } catch (e: Exception) {
            Log.e(TAG, "Error during sync: ${e.message}", e)
        }
    }

    // CREATE - Server assigns ID
    suspend fun insert(recipe: Recipe): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Creating recipe: ${recipe.title}")

            // Try to send to server
            val recipeWithoutId = recipe.copy(recipeId = 0)
            val serverRecipe = api.createRecipe(recipeWithoutId)

            Log.d(TAG, "Recipe created on server with ID: ${serverRecipe.recipeId}")

            // Save with server-assigned ID
            dao.insert(serverRecipe)

            Log.d(TAG, "Recipe saved locally with server ID: ${serverRecipe.recipeId}")
            Result.success("Recipe created successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Server unavailable, saving offline: ${e.message}", e)

            // Save locally with temporary negative ID (to avoid conflicts)
            val tempId = -(System.currentTimeMillis().toInt() % 1000000)
            val offlineRecipe = recipe.copy(recipeId = tempId)
            dao.insert(offlineRecipe)

            // Queue for later sync
            val pendingOp = PendingOperation(
                operationType = OperationType.CREATE,
                recipeId = tempId,
                recipeData = gson.toJson(offlineRecipe)
            )
            pendingDao.insert(pendingOp)

            Log.d(TAG, "Recipe saved locally with temp ID: $tempId (queued for sync)")

            Result.failure(Exception("Saved locally. Will sync when online."))
        }
    }

    // UPDATE - Reuse existing server ID
    suspend fun update(recipe: Recipe): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating recipe ID: ${recipe.recipeId}")

            // Update on server first
            val updatedRecipe = api.updateRecipe(recipe.recipeId, recipe)

            Log.d(TAG, "Recipe updated on server: ${recipe.recipeId}")

            // Update locally
            dao.update(updatedRecipe)

            Log.d(TAG, "Recipe updated locally: ${recipe.recipeId}")
            Result.success("Recipe updated successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update recipe on server: ${e.message}", e)

            // Update locally
            dao.update(recipe)

            // Queue for later sync (only if it's a real server ID, not temporary)
            if (recipe.recipeId > 0) {
                val pendingOp = PendingOperation(
                    operationType = OperationType.UPDATE,
                    recipeId = recipe.recipeId,
                    recipeData = gson.toJson(recipe)
                )
                pendingDao.insert(pendingOp)
                Log.d(TAG, "Recipe updated locally (queued for sync)")
            } else {
                Log.d(TAG, "Recipe updated locally (offline mode, temp ID)")
            }

            Result.failure(Exception("Updated locally. Will sync when online."))
        }
    }

    // DELETE - Send only ID to server
    suspend fun delete(id: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Deleting recipe ID: $id")

            // Delete locally first (for immediate UI feedback)
            dao.deleteById(id)
            Log.d(TAG, "Recipe deleted locally: $id")

            // Try to delete from server
            api.deleteRecipe(id)
            Log.d(TAG, "Recipe deleted from server: $id")

            Result.success("Recipe deleted successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Server unavailable for delete: ${e.message}", e)

            // Already deleted locally above

            // Queue for later sync (only if it's a real server ID)
            if (id > 0) {
                val pendingOp = PendingOperation(
                    operationType = OperationType.DELETE,
                    recipeId = id
                )
                pendingDao.insert(pendingOp)
                Log.d(TAG, "Delete operation queued for sync (ID: $id)")
            } else {
                Log.d(TAG, "Temp ID recipe deleted, no sync needed")
            }

            Result.failure(Exception("Deleted locally. Will sync when online."))
        }
    }

    // ---------------- MODIFICATION START ----------------
// Delete a single pending operation by recipe ID
    suspend fun deletePendingRecipeByRecipeId(recipeId: Int) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Deleting pending operations for recipe ID: $recipeId")
        pendingDao.deleteByRecipeId(recipeId)
    }

// ---------------- MODIFICATION END ----------------


    // Debug methods
    suspend fun getPendingOperations(): List<PendingOperation> = withContext(Dispatchers.IO) {
        pendingDao.getAllPendingOperations()
    }

    suspend fun clearPendingOperations() = withContext(Dispatchers.IO) {
        Log.d(TAG, "Clearing all pending operations")
        pendingDao.clearAll()
    }
}