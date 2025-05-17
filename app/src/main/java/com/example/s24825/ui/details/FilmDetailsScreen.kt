package com.example.s24825.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.filmoteka.R
import com.example.s24825.FilmotekaApplication
import com.example.s24825.data.entity.Film
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FilmDetailsScreen(
    filmId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as FilmotekaApplication
    val viewModel: FilmDetailViewModel = viewModel(
        factory = FilmDetailViewModel.Factory(app.repository, filmId)
    )

    val film by viewModel.film.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = { FilmDetailsTopAppBar(onBackClick) },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> LoadingIndicator()
                film == null -> FilmNotFoundDisplay()
                else -> FilmDetailsContent(film!!, dateFormat) // Wiemy, że film nie jest null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilmDetailsTopAppBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(stringResource(R.string.film_details_screen_title)) },
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

@Composable
private fun BoxScope.LoadingIndicator() { // Używamy BoxScope
    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
}

@Composable
private fun BoxScope.FilmNotFoundDisplay() { // Używamy BoxScope
    Text(
        text = stringResource(R.string.film_details_error_not_found),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .align(Alignment.Center)
            .padding(16.dp)
    )
}

@Composable
private fun FilmDetailsContent(film: Film, dateFormat: SimpleDateFormat) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        PosterDisplay(film.posterPath)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = film.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        InfoText(stringResource(R.string.film_details_label_release_date, dateFormat.format(film.releaseDate)))
        Spacer(modifier = Modifier.height(8.dp))
        InfoText(stringResource(R.string.film_details_label_category, film.category))
        Spacer(modifier = Modifier.height(8.dp))
        StatusAndRatingDisplay(film.isWatched, film.rating)
        film.comment?.takeIf { it.isNotBlank() }?.let { comment ->
            CommentDisplay(comment)
        }
        if (film.isWatched) {
            ReadOnlyMessageDisplay()
        }
    }
}

@Composable
private fun PosterDisplay(posterPath: String?) {
    if (posterPath != null) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(posterPath)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.film_item_poster_description),
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 300.dp)
                .clip(MaterialTheme.shapes.medium)
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.film_details_no_poster),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoText(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun StatusAndRatingDisplay(isWatched: Boolean, rating: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (isWatched) stringResource(R.string.film_details_label_status_watched)
            else stringResource(R.string.film_details_label_status_unwatched),
            style = MaterialTheme.typography.bodyLarge
        )
        if (isWatched && rating != null) {
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = stringResource(R.string.content_desc_star_icon),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.film_details_label_rating, rating.toString()),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CommentDisplay(comment: String) {
    Spacer(modifier = Modifier.height(16.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.film_details_label_comment_section),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = comment, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ReadOnlyMessageDisplay() {
    Spacer(modifier = Modifier.height(24.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = stringResource(R.string.film_details_read_only_message),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
