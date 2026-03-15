package com.example.recipebook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.recipebook.model.PendingOperation
import com.example.recipebook.model.Recipe

@Database(
    entities = [Recipe::class, PendingOperation::class],
    version = 2,
    exportSchema = false
)
abstract class RecipeDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun pendingOperationsDao(): PendingOperationsDao

    companion object {
        @Volatile private var INSTANCE: RecipeDatabase? = null

        fun getDatabase(context: Context): RecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecipeDatabase::class.java,
                    "recipe_database"
                )
                    .fallbackToDestructiveMigration() // For development - removes data on schema change
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}