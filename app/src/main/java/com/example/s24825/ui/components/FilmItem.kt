package com.example.s24825.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow // Zgodnie z kodem, wcześniej było PlayCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.filmoteka.R
import com.example.s24825.data.entity.Film
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Composable for displaying a single film item in the list.
 *
 * This component shows the film's poster, title, release date,
 * category, and watching status/rating. It handles both click
 * and long-click events.
 */
@OptIn(ExperimentalFoundationApi::class) // Potrzebne dla combinedClickable
@Composable
fun FilmItem(
    film: Film,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            // Używamy combinedClickable do obsługi zarówno krótkiego, jak i długiego kliknięcia
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp) // Dodano dla spójności z zaokrągleniem obrazka
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Można rozważyćIntrinsicSize.Min dla wysokości
        ) {
            // Poster Image
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (film.posterPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(film.posterPath)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.film_item_poster_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder when no poster is available
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.film_item_no_poster_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Film details
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween // Rozkłada elementy w kolumnie
            ) {
                Column { // Grupowanie górnych tekstów
                    // Title
                    Text(
                        text = film.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Date
                    Text(
                        text = stringResource(
                            R.string.film_item_label_premiere_date,
                            dateFormat.format(film.releaseDate)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Category
                    Text(
                        text = stringResource(R.string.film_item_label_category, film.category),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status row (watched/unwatched or rating)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (film.isWatched) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.film_item_rating_icon_description),
                            tint = MaterialTheme.colorScheme.tertiary, // Kolor dla obejrzanych/ocenionych
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                R.string.film_item_label_rating,
                                film.rating?.toString() ?: stringResource(R.string.film_item_rating_none)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.film_item_status_unwatched),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary // Kolor dla nieobejrzanych
                        )
                    }
                }
            }
        }
    }
}
