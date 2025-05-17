package com.example.s24825.ui.addedit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.s24825.data.entity.Film
import com.example.s24825.data.entity.FilmCategories
import com.example.s24825.data.repository.FilmRepository
import com.example.s24825.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

/**
 * ViewModel for adding or editing a film.
 *
 * This ViewModel handles the business logic for creating new films or editing
 * existing unwatched films, including validation, image selection, and
 * saving operations.
 */
class FilmEditViewModel(
    private val repository: FilmRepository,
    private val filmId: Long = 0, // 0 indicates a new film
    private val imageUtils: ImageUtils
) : ViewModel() {

    // UI state flows for form fields
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title

    private val _releaseDate = MutableStateFlow(Date())
    val releaseDate: StateFlow<Date> = _releaseDate

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category

    private val _isWatched = MutableStateFlow(false)
    val isWatched: StateFlow<Boolean> = _isWatched

    private val _rating = MutableStateFlow<Int?>(null)
    val rating: StateFlow<Int?> = _rating

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment

    private val _posterPath = MutableStateFlow<String?>(null)
    val posterPath: StateFlow<String?> = _posterPath

    // Loading and error states
    private val _isLoading = MutableStateFlow(filmId > 0)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _validationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val validationErrors: StateFlow<Map<String, String>> = _validationErrors

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private var originalFilm: Film? = null

    init {
        if (filmId > 0) {
            // Load existing film if editing
            loadFilm()
        }
    }

    /**
     * Loads an existing film's data for editing.
     */
    private fun loadFilm() {
        viewModelScope.launch {
            _isLoading.value = true

            repository.getFilmById(filmId)?.let { film ->
                originalFilm = film

                _title.value = film.title
                _releaseDate.value = film.releaseDate
                _category.value = film.category
                _isWatched.value = film.isWatched
                _rating.value = film.rating
                _comment.value = film.comment ?: ""
                _posterPath.value = film.posterPath
            }

            _isLoading.value = false
        }
    }

    /**
     * Updates the title field.
     */
    fun setTitle(title: String) {
        _title.value = title
    }

    /**
     * Updates the release date field.
     */
    fun setReleaseDate(date: Date) {
        _releaseDate.value = date
    }

    /**
     * Updates the category field.
     */
    fun setCategory(category: String) {
        _category.value = category
    }

    /**
     * Updates the watched status field.
     */
    fun setWatched(isWatched: Boolean) {
        _isWatched.value = isWatched
    }

    /**
     * Updates the rating field.
     */
    fun setRating(rating: Int?) {
        _rating.value = rating
    }

    /**
     * Updates the comment field.
     */
    fun setComment(comment: String) {
        _comment.value = comment
    }

    /**
     * Handles selection of a poster image from the gallery.
     * Saves the image to the app's storage and updates the poster path.
     */
    fun selectPoster(uri: Uri) {
        viewModelScope.launch {
            val path = imageUtils.saveImageFromUri(uri)
            _posterPath.value = path
        }
    }

    /**
     * Validates the form data before saving.
     * @return True if all validations pass, false otherwise
     */
    private fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()

        // Title cannot be empty
        if (_title.value.isBlank()) {
            errors["title"] = "Tytuł nie może być pusty"
        }

        // Release date cannot be more than 2 years in the future
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, 2)
        val twoYearsFromNow = calendar.time

        if (_releaseDate.value.after(twoYearsFromNow)) {
            errors["releaseDate"] = "Data premiery nie może być późniejsza niż 2 lata od dziś"
        }

        // Category must be selected
        if (_category.value.isBlank() || !FilmCategories.categories.contains(_category.value)) {
            errors["category"] = "Kategoria musi być wybrana"
        }

        // Rating must be provided if film is marked as watched
        if (_isWatched.value && _rating.value == null) {
            errors["rating"] = "Ocena jest wymagana dla obejrzanych filmów"
        }

        _validationErrors.value = errors
        return errors.isEmpty()
    }

    /**
     * Saves the film (creates a new one or updates an existing one).
     * Performs validation before saving.
     */
    fun saveFilm() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            val film = Film(
                id = filmId,
                title = _title.value,
                releaseDate = _releaseDate.value,
                category = _category.value,
                isWatched = _isWatched.value,
                rating = if (_isWatched.value) _rating.value else null,
                comment = _comment.value.takeIf { it.isNotBlank() },
                posterPath = _posterPath.value
            )

            if (filmId > 0) {
                // Update existing film
                repository.updateFilm(film)
            } else {
                // Create new film
                repository.insertFilm(film)
            }

            _isSaved.value = true
        }
    }

    /**
     * Factory for creating FilmEditViewModel instances with dependencies.
     */
    class Factory(
        private val repository: FilmRepository,
        private val filmId: Long = 0,
        private val imageUtils: ImageUtils
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FilmEditViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FilmEditViewModel(repository, filmId, imageUtils) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}