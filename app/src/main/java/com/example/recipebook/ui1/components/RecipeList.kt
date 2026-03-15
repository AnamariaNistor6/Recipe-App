package com.example.recipebook.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.recipebook.model.Recipe
import com.example.recipebook.ui1.components.RecipeItem

@Composable
fun RecipeList(
    recipes: List<Recipe>,
    onClick: (Recipe) -> Unit,
    onEdit: (Recipe) -> Unit,
    onDelete: (Recipe) -> Unit
) {
    LazyColumn(
        modifier = Modifier,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(recipes, key = { it.recipeId }) { recipe ->
            RecipeItem(
                recipe = recipe,
                onClick = onClick,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    }
}