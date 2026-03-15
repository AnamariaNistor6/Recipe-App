package com.example.recipebook.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipebook.model.Recipe
import com.example.recipebook.ui.components.RecipeList
import com.example.recipebook.ui.theme.RecipeBookTheme
import com.example.recipebook.viewmodel.RecipeViewModel
import android.util.Log
import com.example.recipebook.data.RecipeDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class MainActivity : ComponentActivity() {

    private val viewModel: RecipeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.deletePendingRecipeById(37564) { error ->
            if (error != null) {
                Log.e("MainActivity", "Failed to delete pending recipe: $error")
            } else {
                Log.d("MainActivity", "Pending recipe 37564 deleted successfully")
            }
        }


        setContent {
            RecipeBookTheme {
                val recipes by viewModel.recipes.observeAsState(emptyList())
                var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
                var snackbarMessage by remember { mutableStateOf<String?>(null) }
                val snackbarHostState = remember { SnackbarHostState() }

                // Show snackbar when message changes
                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let { message ->
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Short
                        )
                        snackbarMessage = null
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Recipe Book") })
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            startActivity(
                                Intent(this@MainActivity, AddEditRecipeActivity::class.java)
                            )
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Recipe")
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    ) {
                        if (recipes.isEmpty()) {
                            Text(
                                "No recipes yet. Tap + to add one!",
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            RecipeList(
                                recipes = recipes,
                                onClick = { recipe ->
                                    startActivity(
                                        Intent(this@MainActivity, RecipeDetailActivity::class.java)
                                            .putExtra("recipeId", recipe.recipeId)
                                    )
                                },
                                onEdit = { recipe ->
                                    startActivity(
                                        Intent(this@MainActivity, AddEditRecipeActivity::class.java)
                                            .putExtra("recipeId", recipe.recipeId)
                                    )
                                },
                                onDelete = { recipe ->
                                    recipeToDelete = recipe
                                }
                            )
                        }
                    }
                }

                // Delete confirmation dialog
                recipeToDelete?.let { recipe ->
                    AlertDialog(
                        onDismissRequest = { recipeToDelete = null },
                        title = { Text("Delete Recipe") },
                        text = { Text("Are you sure you want to delete \"${recipe.title}\"?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.deleteRecipe(recipe.recipeId) { error ->
                                        snackbarMessage = error ?: "Recipe deleted successfully"
                                    }
                                    recipeToDelete = null
                                }
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { recipeToDelete = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}