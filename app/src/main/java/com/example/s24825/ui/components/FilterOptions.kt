package com.example.s24825.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.s24825.data.entity.FilmCategories

/**
 * Composable for film list filtering options.
 *
 * This component provides UI for filtering films by category and watch status.
 * It uses Chip-style toggleable buttons for a modern, mobile-friendly interface.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        // Category filters
        Text(
            text = "Kategoria:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            // "All" category filter
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("Wszystkie") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )

            // Specific category filters
            FilmCategories.categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Watch status filters
        Text(
            text = "Status:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            // "All" status filter
            FilterChip(
                selected = selectedWatchStatus == null,
                onClick = { onWatchStatusSelected(null) },
                label = { Text("Wszystkie") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )

            // Watched filter
            FilterChip(
                selected = selectedWatchStatus == true,
                onClick = { onWatchStatusSelected(true) },
                label = { Text("Obejrzane") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )

            // Unwatched filter
            FilterChip(
                selected = selectedWatchStatus == false,
                onClick = { onWatchStatusSelected(false) },
                label = { Text("Nieobejrzane") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}