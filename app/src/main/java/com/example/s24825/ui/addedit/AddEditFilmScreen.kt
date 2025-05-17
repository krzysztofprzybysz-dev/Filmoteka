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

    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectPoster(it) }
    }

    LaunchedEffect(isSaved) {
        if (isSaved) {
            onSaveClick()
        }
    }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val screenTitle = if (filmId > 0) stringResource(R.string.edit_film_screen_title) else stringResource(R.string.add_film_screen_title)

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
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Title field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { viewModel.setTitle(it) },
                        label = { Text(stringResource(R.string.add_edit_label_title)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = validationErrors.containsKey("title"),
                        supportingText = {
                            validationErrors["title"]?.let { errorResId ->
                                Text(text = stringResource(id = errorResId))
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date picker field
                    OutlinedTextField(
                        value = dateFormat.format(releaseDate),
                        onValueChange = { /* Read-only, handled by DatePickerDialog */ },
                        label = { Text(stringResource(R.string.add_edit_label_release_date)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePickerDialog = true }, // Otwiera DatePickerDialog
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = stringResource(R.string.content_desc_date_range_icon),
                                modifier = Modifier.clickable { showDatePickerDialog = true }
                            )
                        },
                        isError = validationErrors.containsKey("releaseDate"),
                        supportingText = {
                            validationErrors["releaseDate"]?.let { errorResId ->
                                Text(text = stringResource(id = errorResId))
                            }
                        }
                    )
                    // DatePickerDialog
                    if (showDatePickerDialog) {
                        val calendar = Calendar.getInstance().apply { time = releaseDate }
                        val datePickerState = rememberDatePickerState(
                            initialSelectedDateMillis = calendar.timeInMillis
                        )
                        DatePickerDialog(
                            onDismissRequest = { showDatePickerDialog = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        viewModel.setReleaseDate(Date(millis))
                                    }
                                    showDatePickerDialog = false
                                }) {
                                    Text(stringResource(android.R.string.ok))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePickerDialog = false }) {
                                    Text(stringResource(android.R.string.cancel))
                                }
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category.ifEmpty { stringResource(R.string.add_edit_label_category) },
                            onValueChange = { },
                            label = { Text(stringResource(R.string.add_edit_label_category)) },
                            modifier = Modifier // Corrected: Only one modifier parameter
                                .fillMaxWidth()
                                .clickable { showCategoryDropdown = true }, // Całe pole klikalne
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = stringResource(R.string.content_desc_arrow_drop_down)
                                )
                            },
                            isError = validationErrors.containsKey("category"),
                            supportingText = {
                                validationErrors["category"]?.let { errorResId ->
                                    Text(text = stringResource(id = errorResId))
                                }
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
                                        viewModel.setCategory(categoryOption)
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Watched status switch
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
                            text = if (isWatched) stringResource(R.string.add_edit_status_watched) else stringResource(R.string.add_edit_status_unwatched),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isWatched,
                            onCheckedChange = { viewModel.setWatched(it) }
                        )
                    }

                    // Rating field (only shown when watched is true)
                    if (isWatched) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = rating?.toString() ?: "",
                            onValueChange = {
                                val newRating = it.toIntOrNull()
                                if (it.isEmpty()) {
                                    viewModel.setRating(null)
                                } else if (newRating != null && newRating in 1..10) {
                                    viewModel.setRating(newRating)
                                } else if (newRating == null && it.length <= 2) {
                                    // Allow typing if not a valid number yet but not too long
                                }
                            },
                            label = { Text(stringResource(R.string.add_edit_label_rating)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = validationErrors.containsKey("rating"),
                            supportingText = {
                                validationErrors["rating"]?.let { errorResId ->
                                    Text(text = stringResource(id = errorResId))
                                } ?: Text(stringResource(R.string.add_edit_hint_rating))
                            },
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Comment field
                    OutlinedTextField(
                        value = comment ?: "",
                        onValueChange = { viewModel.setComment(it) },
                        label = { Text(stringResource(R.string.add_edit_label_comment)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Poster selection
                    Text(
                        text = stringResource(R.string.add_edit_label_poster),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp)) // Corrected from Modifier.height
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
                            .clickable { imagePicker.launch("image/*") },
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

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.saveFilm() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text(stringResource(R.string.add_edit_save_button))
                    }
                }
            }
        }
    }
}
