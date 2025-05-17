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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.filmoteka.R
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
    // Można dodać stan isLoading, jeśli operacje filtrowania/ładowania są długie
    // val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.film_list_title)) },
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
                    contentDescription = stringResource(id = R.string.fab_add_film_description),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
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
            Text(
                text = stringResource(id = R.string.item_count, itemCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Film list or empty state
            // if (isLoading) {
            //     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            //         CircularProgressIndicator()
            //     }
            // } else
            if (films.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.no_films_matching),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 80.dp) // Padding dla FAB i estetyki
                ) {
                    items(films, key = { film -> film.id }) { film ->
                        FilmItem(
                            film = film,
                            onClick = { onFilmClick(film.id, film.isWatched) },
                            onLongClick = { viewModel.showDeleteDialog(film) } // Przekazanie funkcji do obsługi długiego kliknięcia
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
            title = { Text(stringResource(id = R.string.delete_dialog_title)) },
            text = { Text(stringResource(id = R.string.delete_dialog_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteFilm() }) {
                    Text(stringResource(id = R.string.delete_dialog_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text(stringResource(id = R.string.delete_dialog_dismiss_button))
                }
            }
        )
    }
}
