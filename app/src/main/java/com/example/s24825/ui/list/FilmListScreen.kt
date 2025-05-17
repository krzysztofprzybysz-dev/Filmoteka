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
import com.example.filmoteka.R // ZASTĄP POPRAWNYM IMPORTEM R
import com.example.s24825.FilmotekaApplication
import com.example.s24825.data.entity.Film
import com.example.s24825.ui.components.FilterOptions
import com.example.s24825.ui.components.FilmItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmListScreen(
    onFilmClick: (Long, Boolean) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as FilmotekaApplication
    val viewModel: FilmListViewModel = viewModel(
        factory = FilmListViewModel.Factory(app.repository)
    )

    val films by viewModel.filteredFilms.collectAsState(initial = emptyList())
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedWatchStatus by viewModel.selectedWatchStatus.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val filmToDelete by viewModel.filmToDelete.collectAsState() // Dodajemy stan dla filmu do usunięcia
    val itemCount by viewModel.itemCount.collectAsState()

    Scaffold(
        topBar = { FilmListTopAppBar() },
        floatingActionButton = { FilmListFloatingActionButton(onAddClick) },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FilterOptions(
                selectedCategory = selectedCategory,
                selectedWatchStatus = selectedWatchStatus,
                onCategorySelected = viewModel::setCategory,
                onWatchStatusSelected = viewModel::setWatchStatus
            )

            ItemCountDisplay(itemCount)

            if (films.isEmpty()) {
                EmptyListDisplay()
            } else {
                FilmsLazyList(
                    films = films,
                    onFilmClick = onFilmClick,
                    onFilmLongClick = viewModel::showDeleteDialog // Przekazujemy referencję do funkcji
                )
            }
        }
    }

    if (showDeleteDialog && filmToDelete != null) {
        DeleteConfirmationDialog(
            filmTitle = filmToDelete!!.title, // Bezpieczne odwołanie, bo sprawdzamy showDeleteDialog && filmToDelete != null
            onConfirm = {
                viewModel.deleteFilm()
                viewModel.dismissDeleteDialog() // Ukryj dialog po potwierdzeniu
            },
            onDismiss = viewModel::dismissDeleteDialog
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilmListTopAppBar() {
    TopAppBar(
        title = { Text(stringResource(id = R.string.film_list_title)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun FilmListFloatingActionButton(onAddClick: () -> Unit) {
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

@Composable
private fun ItemCountDisplay(itemCount: Int) {
    Text(
        text = stringResource(id = R.string.item_count, itemCount),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ColumnScope.EmptyListDisplay() { // Używamy ColumnScope, aby poprawnie użyć weight
    Box(
        modifier = Modifier
            .weight(1f) // Aby Box wypełnił dostępną przestrzeń w Column
            .fillMaxWidth()
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
}

@Composable
private fun FilmsLazyList(
    films: List<Film>,
    onFilmClick: (Long, Boolean) -> Unit,
    onFilmLongClick: (Film) -> Unit // Zmieniono parametr na (Film) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 80.dp)
    ) {
        items(films, key = { film -> film.id }) { film ->
            FilmItem(
                film = film,
                onClick = { onFilmClick(film.id, film.isWatched) },
                onLongClick = { onFilmLongClick(film) } // Przekazujemy cały obiekt Film
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    filmTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.delete_dialog_title)) },
        text = {
            Text(
                stringResource(
                    id = R.string.delete_dialog_message_with_title, // Nowy string zasobu
                    filmTitle
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(id = R.string.delete_dialog_confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.delete_dialog_dismiss_button))
            }
        }
    )
}
