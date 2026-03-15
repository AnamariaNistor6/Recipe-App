package com.example.recipebook.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.recipebook.model.Recipe
import com.example.recipebook.ui.theme.RecipeBookTheme
import com.example.recipebook.viewmodel.RecipeViewModel

class AddEditRecipeActivity : ComponentActivity() {

    private val viewModel: RecipeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recipeId = intent.getIntExtra("recipeId", -1).takeIf { it != -1 }

        setContent {
            RecipeBookTheme {
                val recipe by if (recipeId != null) {
                    viewModel.getRecipeById(recipeId).observeAsState()
                } else {
                    remember { mutableStateOf<Recipe?>(null) }
                }

                var title by remember(recipe) { mutableStateOf(recipe?.title ?: "") }
                var ingredients by remember(recipe) { mutableStateOf(recipe?.ingredients ?: "") }
                var instructions by remember(recipe) { mutableStateOf(recipe?.instructions ?: "") }
                var prepTime by remember(recipe) { mutableStateOf(recipe?.prepTimeMinutes?.toString() ?: "") }
                var servings by remember(recipe) { mutableStateOf(recipe?.servings?.toString() ?: "") }

                var titleError by remember { mutableStateOf(false) }
                var ingredientsError by remember { mutableStateOf(false) }
                var instructionsError by remember { mutableStateOf(false) }
                var prepTimeError by remember { mutableStateOf(false) }
                var servingsError by remember { mutableStateOf(false) }

                var snackbarMessage by remember { mutableStateOf<String?>(null) }
                val snackbarHostState = remember { SnackbarHostState() }
                var isSaving by remember { mutableStateOf(false) }

                // Show snackbar when message changes
                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let { message ->
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Long
                        )
                        snackbarMessage = null
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(if (recipeId == null) "Add Recipe" else "Edit Recipe") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                titleError = false
                            },
                            label = { Text("Recipe Title") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = titleError,
                            supportingText = if (titleError) {
                                { Text("Title is required") }
                            } else null,
                            enabled = !isSaving
                        )

                        OutlinedTextField(
                            value = ingredients,
                            onValueChange = {
                                ingredients = it
                                ingredientsError = false
                            },
                            label = { Text("Ingredients (one per line)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            isError = ingredientsError,
                            supportingText = if (ingredientsError) {
                                { Text("Ingredients are required") }
                            } else null,
                            enabled = !isSaving,
                            maxLines = 6
                        )

                        OutlinedTextField(
                            value = instructions,
                            onValueChange = {
                                instructions = it
                                instructionsError = false
                            },
                            label = { Text("Instructions") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            isError = instructionsError,
                            supportingText = if (instructionsError) {
                                { Text("Instructions are required") }
                            } else null,
                            enabled = !isSaving,
                            maxLines = 8
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = prepTime,
                                onValueChange = {
                                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                        prepTime = it
                                        prepTimeError = false
                                    }
                                },
                                label = { Text("Prep Time (min)") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = prepTimeError,
                                supportingText = if (prepTimeError) {
                                    { Text("Valid time required") }
                                } else null,
                                enabled = !isSaving
                            )

                            OutlinedTextField(
                                value = servings,
                                onValueChange = {
                                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                        servings = it
                                        servingsError = false
                                    }
                                },
                                label = { Text("Servings") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = servingsError,
                                supportingText = if (servingsError) {
                                    { Text("Valid servings required") }
                                } else null,
                                enabled = !isSaving
                            )
                        }

                        Button(
                            onClick = {
                                // Validate all fields
                                titleError = title.isBlank()
                                ingredientsError = ingredients.isBlank()
                                instructionsError = instructions.isBlank()
                                prepTimeError = prepTime.toIntOrNull()?.let { it <= 0 } ?: true
                                servingsError = servings.toIntOrNull()?.let { it <= 0 } ?: true

                                if (!titleError && !ingredientsError && !instructionsError &&
                                    !prepTimeError && !servingsError) {

                                    isSaving = true

                                    val newRecipe = Recipe(
                                        recipeId = recipe?.recipeId ?: 0,
                                        title = title.trim(),
                                        ingredients = ingredients.trim(),
                                        instructions = instructions.trim(),
                                        prepTimeMinutes = prepTime.toInt(),
                                        servings = servings.toInt()
                                    )

                                    if (recipe == null) {
                                        viewModel.insertRecipe(newRecipe) { error ->
                                            isSaving = false
                                            if (error != null) {
                                                snackbarMessage = error
                                            } else {
                                                finish()
                                            }
                                        }
                                    } else {
                                        viewModel.updateRecipe(newRecipe) { error ->
                                            isSaving = false
                                            if (error != null) {
                                                snackbarMessage = error
                                            } else {
                                                finish()
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(if (recipe == null) "Add Recipe" else "Update Recipe")
                        }
                    }
                }
            }
        }
    }
}