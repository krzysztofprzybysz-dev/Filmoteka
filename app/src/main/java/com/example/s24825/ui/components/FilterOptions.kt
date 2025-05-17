package com.example.s24825.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.filmoteka.R
import com.example.s24825.data.entity.FilmCategories


@Composable
fun FilterOptions(
    selectedCategory: String?,
    selectedWatchStatus: Boolean?,
    onCategorySelected: (String?) -> Unit,
    onWatchStatusSelected: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        CategoryFilters(selectedCategory, onCategorySelected)
        Spacer(modifier = Modifier.height(12.dp))
        WatchStatusFilters(selectedWatchStatus, onWatchStatusSelected)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilters(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val categoryOptions = remember {
        listOf(null to R.string.filter_option_all_categories) + // Używamy ID zasobu
                FilmCategories.categories.map { category ->
                    category to when (category) { // Mapowanie na ID zasobów dla kategorii
                        FilmCategories.FILM -> R.string.category_film
                        FilmCategories.SERIAL -> R.string.category_serial
                        FilmCategories.DOKUMENT -> R.string.category_documentary
                        else -> 0 // Lub jakiś domyślny string, jeśli kategorie są dynamiczne
                    }
                }
    }

    Text(
        text = stringResource(R.string.filter_label_category),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(4.dp))
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(categoryOptions, key = { it.first ?: "all_cat_key" }) { (categoryValue, categoryLabelResId) ->
            if (categoryLabelResId != 0) { // Sprawdzamy, czy mamy poprawny ID zasobu
                FilterChip(
                    selected = selectedCategory == categoryValue,
                    onClick = { onCategorySelected(categoryValue) },
                    label = { Text(stringResource(id = categoryLabelResId)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WatchStatusFilters(
    selectedWatchStatus: Boolean?,
    onWatchStatusSelected: (Boolean?) -> Unit
) {
    val watchStatusOptions = remember {
        listOf(
            null to R.string.filter_option_all_statuses,
            true to R.string.filter_option_watched,
            false to R.string.filter_option_unwatched
        )
    }

    Text(
        text = stringResource(R.string.filter_label_status),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(4.dp))
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(watchStatusOptions, key = { it.first?.toString() ?: "all_stat_key" }) { (statusValue, statusLabelResId) ->
            FilterChip(
                selected = selectedWatchStatus == statusValue,
                onClick = { onWatchStatusSelected(statusValue) },
                label = { Text(stringResource(id = statusLabelResId)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
