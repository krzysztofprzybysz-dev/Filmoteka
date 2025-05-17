package com.example.s24825.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.filmoteka.R // ZASTĄP POPRAWNYM IMPORTEM R TWOJEGO PROJEKTU
import com.example.s24825.data.entity.Film
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilmItem(
    film: Film,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Używamy remember, aby uniknąć tworzenia obiektu przy każdej rekompozycji
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // Dopasowuje wysokość do zawartości, ale może wymagać testów
        ) {
            FilmItemPoster(film.posterPath, context)

            Column(
                modifier = Modifier
                    .weight(1f) // Pozwala tej kolumnie zająć pozostałą przestrzeń
                    .fillMaxHeight() // Wypełnia wysokość wiersza
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween // Rozkłada elementy
            ) {
                FilmItemTextInfo(film = film, dateFormat = dateFormat)
                FilmItemStatusOrRatingInfo(film = film)
            }
        }
    }
}

@Composable
private fun FilmItemPoster(posterPath: String?, context: android.content.Context) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight() // Wypełnia wysokość wiersza
            .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (posterPath != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(posterPath)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.film_item_poster_description),
                contentScale = ContentScale.Crop, // Crop może lepiej wypełnić przestrzeń
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.film_item_no_poster_description),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
private fun FilmItemTextInfo(film: Film, dateFormat: SimpleDateFormat) {
    Column {
        Text(
            text = film.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp)) // Mały odstęp
        Text(
            text = stringResource(
                R.string.film_item_label_premiere_date,
                dateFormat.format(film.releaseDate)
            ),
            style = MaterialTheme.typography.bodySmall, // Mniejszy tekst dla daty i kategorii
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.film_item_label_category, film.category),
            style = MaterialTheme.typography.bodySmall, // Mniejszy tekst
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilmItemStatusOrRatingInfo(film: Film) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp) // Odstęp od górnych tekstów
    ) {
        if (film.isWatched) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = stringResource(R.string.film_item_rating_icon_description),
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp) // Nieco mniejsza ikona
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(
                    R.string.film_item_label_rating,
                    film.rating?.toString() ?: stringResource(R.string.film_item_rating_none)
                ),
                style = MaterialTheme.typography.labelLarge, // Użycie labelLarge dla statusu/oceny
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = stringResource(R.string.film_item_status_unwatched),
                style = MaterialTheme.typography.labelLarge, // Użycie labelLarge
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
