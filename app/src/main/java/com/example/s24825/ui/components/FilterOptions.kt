package com.example.s24825.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.filmoteka.R
import com.example.s24825.data.entity.FilmCategories

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterOptions(
    selectedCategory: String?,
    selectedWatchStatus: Boolean?,
    onCategorySelected: (String?) -> Unit,
    onWatchStatusSelected: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    val watchStatusOptions = listOf(
        null to stringResource(R.string.filter_option_all_statuses),
        true to stringResource(R.string.filter_option_watched),
        false to stringResource(R.string.filter_option_unwatched)
    )

    // Dodajemy "Wszystkie" na początek listy kategorii
    val categoryOptions = listOf(null to stringResource(R.string.filter_option_all_categories)) +
            FilmCategories.categories.map { it to it } // Zakładamy, że nazwy kategorii są OK jako klucze i etykiety

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Category filters
        Text(
            text = stringResource(R.string.filter_label_category),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow( // Użycie LazyRow dla przewijania, jeśli kategorii jest wiele
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(categoryOptions, key = { it.first ?: "all_cat" }) { (categoryValue, categoryLabel) ->
                FilterChip(
                    selected = selectedCategory == categoryValue,
                    onClick = { onCategorySelected(categoryValue) },
                    label = { Text(categoryLabel) }, // Używamy przetłumaczonej etykiety
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White // Lub MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp)) // Zwiększony odstęp

        // Watch status filters
        Text(
            text = stringResource(R.string.filter_label_status),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow( // Użycie LazyRow dla przewijania
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(watchStatusOptions, key = { it.first?.toString() ?: "all_stat" }) { (statusValue, statusLabel) ->
                FilterChip(
                    selected = selectedWatchStatus == statusValue,
                    onClick = { onWatchStatusSelected(statusValue) },
                    label = { Text(statusLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White // Lub MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    }
}
