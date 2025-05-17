package com.example.s24825.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.s24825.FilmotekaApplication
import com.example.s24825.ui.components.FilterOptions
import com.example.s24825.ui.components.FilmItem



@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun FilmListScreen(

    onFilmClick: (Long, Boolean) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier

) {
    // Get ViewModel
    val context = LocalContext.current
    val app = context.applicationContext as FilmotekaApplication
    val viewModel: FilmListViewModel = viewModel(
        factory = FilmListViewModel.Factory(app.repository)
    )

    // Collect state
    val films by viewModel.filteredFilms.collectAsState(initial = emptyList())
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedWatchStatus by viewModel.selectedWatchStatus.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val itemCount by viewModel.itemCount.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filmoteka") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Dodaj film",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )


    { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter options
            FilterOptions(
                selectedCategory = selectedCategory,
                selectedWatchStatus = selectedWatchStatus,
                onCategorySelected = { viewModel.setCategory(it) },
                onWatchStatusSelected = { viewModel.setWatchStatus(it) }
            )

            // Item count
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Liczba pozycji: $itemCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Film list
            if (films.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Brak filmów spełniających wybrane kryteria",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Extra padding for FAB
                ) {
                    items(films) { film ->
                        FilmItem(
                            film = film,
                            onClick = { onFilmClick(film.id, film.isWatched) },
                            onLongClick = { viewModel.showDeleteDialog(film) }
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Potwierdź usunięcie") },
            text = { Text("Czy na pewno chcesz usunąć ten film z kolekcji?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteFilm() }) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Anuluj")
                }
            }
        )
    }
}
