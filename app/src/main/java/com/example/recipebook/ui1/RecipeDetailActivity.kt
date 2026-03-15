package com.example.recipebook.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.recipebook.ui.theme.RecipeBookTheme
import com.example.recipebook.viewmodel.RecipeViewModel

class RecipeDetailActivity : ComponentActivity() {

    private val viewModel: RecipeViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recipeId = intent.getIntExtra("recipeId", -1)

        setContent {
            RecipeBookTheme {
                val recipe by viewModel.getRecipeById(recipeId).observeAsState()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Recipe Details") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    startActivity(
                                        Intent(this@RecipeDetailActivity, AddEditRecipeActivity::class.java)
                                            .putExtra("recipeId", recipeId)
                                    )
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Recipe")
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        recipe?.let {
                            Text(it.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Prep Time", style = MaterialTheme.typography.labelSmall)
                                        Text("${it.prepTimeMinutes} min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Servings", style = MaterialTheme.typography.labelSmall)
                                        Text("${it.servings}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Divider()

                            Column {
                                Text("Ingredients", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                    Text(it.ingredients, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
                                }
                            }

                            Column {
                                Text("Instructions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                    Text(it.instructions, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
                                }
                            }
                        } ?: run {
                            Text("Recipe not found")
                        }
                    }
                }
            }
        }
    }
}
