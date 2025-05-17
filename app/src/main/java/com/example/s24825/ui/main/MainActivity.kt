package com.example.s24825.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.s24825.ui.addedit.AddEditFilmScreen
import com.example.s24825.ui.details.FilmDetailsScreen
import com.example.s24825.ui.list.FilmListScreen
import com.example.s24825.ui.theme.FilmotekaTheme


//bazowa klasa do aktywnosci dziedziczy po klasie ComponentActivity zeby moc uzywac composable z Jetpack compose
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FilmotekaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FilmotekaApp()
                }
            }
        }
    }
}


object FilmotekaDestinations {
    const val FILM_LIST_ROUTE = "film_list"
    const val FILM_DETAILS_ROUTE = "film_details"
    const val FILM_EDIT_ROUTE = "film_edit"
    const val FILM_ADD_ROUTE = "film_add"

    // Route with parameters
    const val FILM_DETAILS_ROUTE_WITH_ARGS = "film_details/{filmId}"
    const val FILM_EDIT_ROUTE_WITH_ARGS = "film_edit/{filmId}"

    // Arguments
    const val FILM_ID_ARG = "filmId"
}

@Composable
fun FilmotekaApp(navController: NavHostController = rememberNavController()) {


    NavHost(
        navController = navController,
        startDestination = FilmotekaDestinations.FILM_LIST_ROUTE
    ) {
        // Film List Screen
        composable(FilmotekaDestinations.FILM_LIST_ROUTE) {
            FilmListScreen(
                onFilmClick = { filmId, isWatched ->
                    if (isWatched) {
                        navController.navigate("${FilmotekaDestinations.FILM_DETAILS_ROUTE}/$filmId")
                    } else {
                        navController.navigate("${FilmotekaDestinations.FILM_EDIT_ROUTE}/$filmId")
                    }
                },
                onAddClick = {
                    navController.navigate(FilmotekaDestinations.FILM_ADD_ROUTE)
                }
            )
        }

        composable(route = FilmotekaDestinations.FILM_DETAILS_ROUTE_WITH_ARGS,
            arguments = listOf(navArgument(FilmotekaDestinations.FILM_ID_ARG) { type = NavType.LongType }))


        { backStackEntry ->
            val filmId = backStackEntry.arguments?.getLong(FilmotekaDestinations.FILM_ID_ARG) ?: 0L

            FilmDetailsScreen(
                filmId = filmId,
                onBackClick = { navController.popBackStack() })
        }

        // Film Edit Screen (For unwatched films)
        composable(
            route = FilmotekaDestinations.FILM_EDIT_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(FilmotekaDestinations.FILM_ID_ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val filmId = backStackEntry.arguments?.getLong(FilmotekaDestinations.FILM_ID_ARG) ?: 0L
            AddEditFilmScreen(
                filmId = filmId,
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveClick = {
                    navController.popBackStack()
                }
            )
        }

        // Film Add Screen (For new films)
        composable(FilmotekaDestinations.FILM_ADD_ROUTE) {
            AddEditFilmScreen(
                filmId = 0L, // 0 means new film
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}