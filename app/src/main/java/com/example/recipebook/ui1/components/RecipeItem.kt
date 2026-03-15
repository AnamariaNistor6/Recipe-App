package com.example.recipebook.ui1.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.recipebook.model.Recipe

@Composable
fun RecipeItem(
    recipe: Recipe,
    onClick: (Recipe) -> Unit,
    onEdit: (Recipe) -> Unit,
    onDelete: (Recipe) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(recipe) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Prep: ${recipe.prepTimeMinutes} min | Serves: ${recipe.servings}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row {
                IconButton(onClick = { onEdit(recipe) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Recipe",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { onDelete(recipe) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Recipe",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}