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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    // Get ViewModel
    val context = LocalContext.current
    val app = context.applicationContext as FilmotekaApplication
    val viewModel: FilmEditViewModel = viewModel(
        factory = FilmEditViewModel.Factory(app.repository, filmId, app.imageUtils)
    )

    // Collect state
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

    // Date formatting
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    // Handle image selection
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectPoster(it) }
    }

    // Handle navigation after save
    LaunchedEffect(isSaved) {
        if (isSaved) {
            onSaveClick()
        }
    }

    // Category dropdown state
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (filmId > 0) "Edytuj film" else "Dodaj nowy film")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Powrót"
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
                // Loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Form content
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
                        label = { Text("Tytuł") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = validationErrors.containsKey("title"),
                        supportingText = {
                            if (validationErrors.containsKey("title")) {
                                Text(text = validationErrors["title"] ?: "")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date picker field
                    OutlinedTextField(
                        value = dateFormat.format(releaseDate),
                        onValueChange = { },
                        label = { Text("Data premiery") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,  // Changed from CalendarMonth to DateRange
                                contentDescription = "Wybierz datę"
                            )
                        },
                        isError = validationErrors.containsKey("releaseDate"),
                        supportingText = {
                            if (validationErrors.containsKey("releaseDate")) {
                                Text(text = validationErrors["releaseDate"] ?: "")
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category dropdown
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { },
                            label = { Text("Kategoria") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Wybierz kategorię"
                                )
                            },
                            isError = validationErrors.containsKey("category"),
                            supportingText = {
                                if (validationErrors.containsKey("category")) {
                                    Text(text = validationErrors["category"] ?: "")
                                }
                            }
                        )

                        // Invisible clickable overlay to open dropdown
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showCategoryDropdown = true }
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
                            text = "Status",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = if (isWatched) "Obejrzany" else "Nieobejrzany",
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
                                if (newRating == null || (newRating in 1..10)) {
                                    viewModel.setRating(newRating)
                                }
                            },
                            label = { Text("Ocena (1-10)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = validationErrors.containsKey("rating"),
                            supportingText = {
                                if (validationErrors.containsKey("rating")) {
                                    Text(text = validationErrors["rating"] ?: "")
                                } else {
                                    Text("Wprowadź ocenę od 1 do 10")
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Comment field
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { viewModel.setComment(it) },
                        label = { Text("Komentarz") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Poster selection
                    Text(
                        text = "Plakat",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Poster preview or selection button
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
                            // Show selected poster
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(posterPath)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Film Poster",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Show selection prompt
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,  // Changed from PlayCircle to PlayArrow
                                    contentDescription = "No Poster",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(40.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Wybierz plakat z galerii",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save button
                    Button(
                        onClick = { viewModel.saveFilm() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Zapisz")
                    }
                }
            }
        }
    }
}