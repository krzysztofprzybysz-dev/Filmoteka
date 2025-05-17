package com.example.s24825.ui.addedit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.filmoteka.R
import com.example.s24825.FilmotekaApplication
import com.example.s24825.data.entity.FilmCategories
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFilmScreen(
    filmId: Long,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as FilmotekaApplication
    val viewModel: FilmEditViewModel = viewModel(
        factory = FilmEditViewModel.Factory(app.repository, filmId, app.imageUtils, app)
    )

    // Stany pobierane z ViewModelu
    val title by viewModel.title.collectAsState()
    val releaseDate by viewModel.releaseDate.collectAsState()
    val category by viewModel.category.collectAsState()
    val isWatched by viewModel.isWatched.collectAsState()
    val rating by viewModel.rating.collectAsState()
    val comment by viewModel.comment.collectAsState()
    val posterPath by viewModel.posterPath.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val validationErrors by viewModel.validationErrors.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

    // Formatter daty
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    // Launcher do wybierania obrazu
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectPoster(it) }
    }

    // Efekt uruchamiany po zapisaniu filmu
    LaunchedEffect(isSaved) {
        if (isSaved) {
            onSaveClick()
        }
    }

    // Tytuł ekranu w zależności od trybu (dodawanie/edycja)
    val screenTitle = if (filmId > 0) stringResource(R.string.edit_film_screen_title)
    else stringResource(R.string.add_film_screen_title)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_arrow_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                // Wskaźnik ładowania
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                // Główna kolumna formularza
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Poszczególne pola formularza jako osobne komponenty
                    TitleInputField(title, viewModel::setTitle, validationErrors["title"])
                    Spacer(modifier = Modifier.height(16.dp))
                    ReleaseDateInputField(releaseDate, viewModel::setReleaseDate, validationErrors["releaseDate"], dateFormat)
                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryInputField(category, viewModel::setCategory, validationErrors["category"])
                    Spacer(modifier = Modifier.height(16.dp))
                    WatchedStatusSwitch(isWatched, viewModel::setWatched)
                    if (isWatched) {
                        Spacer(modifier = Modifier.height(16.dp))
                        RatingInputField(rating, viewModel::setRating, validationErrors["rating"])
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    CommentInputField(comment, viewModel::setComment)
                    Spacer(modifier = Modifier.height(16.dp))
                    PosterInputField(posterPath, imagePicker::launch)
                    Spacer(modifier = Modifier.height(24.dp))
                    SaveButton(isLoading, viewModel::saveFilm)
                }
            }
        }
    }
}

// Prywatne funkcje Composable dla każdego pola formularza
// To czyni główną funkcję AddEditFilmScreen bardziej czytelną

@Composable
private fun TitleInputField(
    title: String,
    onTitleChange: (String) -> Unit,
    errorResId: Int?
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text(stringResource(R.string.add_edit_label_title)) },
        modifier = Modifier.fillMaxWidth(),
        isError = errorResId != null,
        supportingText = {
            errorResId?.let { Text(text = stringResource(id = it)) }
        },
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReleaseDateInputField(
    releaseDate: Date,
    onReleaseDateChange: (Date) -> Unit,
    errorResId: Int?,
    dateFormat: SimpleDateFormat
) {
    var showDatePickerDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = dateFormat.format(releaseDate),
        onValueChange = { /* Read-only */ },
        label = { Text(stringResource(R.string.add_edit_label_release_date)) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePickerDialog = true },
        readOnly = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = stringResource(R.string.content_desc_date_range_icon),
                modifier = Modifier.clickable { showDatePickerDialog = true }
            )
        },
        isError = errorResId != null,
        supportingText = {
            errorResId?.let { Text(text = stringResource(id = it)) }
        }
    )

    if (showDatePickerDialog) {
        val currentCalendar = Calendar.getInstance().apply { time = releaseDate }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentCalendar.timeInMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onReleaseDateChange(Date(it)) }
                    showDatePickerDialog = false
                }) { Text(stringResource(android.R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) { Text(stringResource(android.R.string.cancel)) }
            }
        ) {
            DatePicker(
                state = datePickerState,
                dateValidator = { timestamp ->
                    val maxDateCalendar = Calendar.getInstance().apply { add(Calendar.YEAR, 2) }
                    timestamp <= maxDateCalendar.timeInMillis
                }
            )
        }
    }
}

@Composable
private fun CategoryInputField(
    category: String,
    onCategoryChange: (String) -> Unit,
    errorResId: Int?
) {
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = category,
            onValueChange = { /* Read-only */ },
            label = { Text(stringResource(R.string.add_edit_label_category)) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCategoryDropdown = true },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(R.string.content_desc_arrow_drop_down)
                )
            },
            isError = errorResId != null,
            supportingText = {
                errorResId?.let { Text(text = stringResource(id = it)) }
            }
        )
        DropdownMenu(
            expanded = showCategoryDropdown,
            onDismissRequest = { showCategoryDropdown = false }
        ) {
            FilmCategories.categories.forEach { categoryOption ->
                DropdownMenuItem(
                    text = { Text(categoryOption) },
                    onClick = {
                        onCategoryChange(categoryOption)
                        showCategoryDropdown = false
                    }
                )
            }
        }
    }
}

@Composable
private fun WatchedStatusSwitch(
    isWatched: Boolean,
    onWatchedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.add_edit_label_status),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (isWatched) stringResource(R.string.add_edit_status_watched)
            else stringResource(R.string.add_edit_status_unwatched),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = isWatched, onCheckedChange = onWatchedChange)
    }
}

@Composable
private fun RatingInputField(
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    errorResId: Int?
) {
    OutlinedTextField(
        value = rating?.toString() ?: "",
        onValueChange = {
            val newRating = it.toIntOrNull()
            if (it.isEmpty()) onRatingChange(null)
            else if (newRating != null && newRating in 1..10) onRatingChange(newRating)
            else if (newRating == null && it.length <= 2) { /* Allow intermediate typing */ }
        },
        label = { Text(stringResource(R.string.add_edit_label_rating)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        isError = errorResId != null,
        supportingText = {
            errorResId?.let { Text(text = stringResource(id = it)) }
                ?: Text(stringResource(R.string.add_edit_hint_rating))
        },
        singleLine = true
    )
}

@Composable
private fun CommentInputField(
    comment: String?,
    onCommentChange: (String) -> Unit
) {
    OutlinedTextField(
        value = comment ?: "",
        onValueChange = onCommentChange,
        label = { Text(stringResource(R.string.add_edit_label_comment)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
}

@Composable
private fun PosterInputField(
    posterPath: String?,
    onPosterSelectRequest: (String) -> Unit // Zmieniono typ launchera
) {
    Text(
        text = stringResource(R.string.add_edit_label_poster),
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onPosterSelectRequest("image/*") }, // Wywołanie przekazanej funkcji
        contentAlignment = Alignment.Center
    ) {
        if (posterPath != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(posterPath)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.film_item_poster_description),
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.content_desc_play_arrow_icon),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.add_edit_select_poster_from_gallery),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SaveButton(
    isLoading: Boolean,
    onSaveClick: () -> Unit
) {
    Button(
        onClick = onSaveClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading
    ) {
        Text(stringResource(R.string.add_edit_save_button))
    }
}
