package com.example.s24825.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.s24825.data.entity.Film
import com.example.s24825.data.repository.FilmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the film detail screen.
 *
 * This ViewModel handles loading and displaying a single film's details.
 * It's used for the read-only view of a film (particularly watched films).
 */
class FilmDetailViewModel(
    private val repository: FilmRepository,
    private val filmId: Long
) : ViewModel() {

    private val _film = MutableStateFlow<Film?>(null)
    val film: StateFlow<Film?> = _film

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadFilm()
    }

    /**
     * Loads the film details from the repository.
     */
    private fun loadFilm() {
        viewModelScope.launch {
            _isLoading.value = true
            _film.value = repository.getFilmById(filmId)
            _isLoading.value = false
        }
    }

    /**
     * Factory for creating FilmDetailViewModel instances with dependencies.
     */
    class Factory(
        private val repository: FilmRepository,
        private val filmId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FilmDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FilmDetailViewModel(repository, filmId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}