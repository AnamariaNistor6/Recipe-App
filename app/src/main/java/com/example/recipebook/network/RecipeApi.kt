package com.example.recipebook.network

import com.example.recipebook.model.Recipe
import retrofit2.http.*

interface RecipeApi {

    @GET("recipes")
    suspend fun getRecipes(): List<Recipe>

    @POST("recipes")
    suspend fun createRecipe(@Body recipe: Recipe): Recipe

    @PUT("recipes/{id}")
    suspend fun updateRecipe(
        @Path("id") id: Int,
        @Body recipe: Recipe
    ): Recipe

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: Int)
}
