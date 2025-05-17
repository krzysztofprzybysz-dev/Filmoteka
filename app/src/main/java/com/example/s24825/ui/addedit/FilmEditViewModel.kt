package com.example.s24825.ui.addedit

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.filmoteka.R
import com.example.s24825.data.entity.Film
import com.example.s24825.data.entity.FilmCategories
import com.example.s24825.data.repository.FilmRepository
import com.example.s24825.util.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class FilmEditViewModel(
    private val repository: FilmRepository,
    private val filmId: Long = 0,
    private val imageUtils: ImageUtils,
    private val application: Application
) : ViewModel() {

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

    private val _comment = MutableStateFlow<String?>("")
    val comment: StateFlow<String?> = _comment

    private val _posterPath = MutableStateFlow<String?>(null)
    val posterPath: StateFlow<String?> = _posterPath

    private val _isLoading = MutableStateFlow(filmId > 0)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _validationErrors = MutableStateFlow<Map<String, Int>>(emptyMap())
    val validationErrors: StateFlow<Map<String, Int>> = _validationErrors

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    private var originalFilm: Film? = null

    init {
        if (filmId > 0) {
            loadFilm()
        } else {
            // Ustaw domyślną kategorię, jeśli to nowy film, np. pierwszą z listy
            // _category.value = FilmCategories.categories.firstOrNull() ?: ""
            _isLoading.value = false // Dla nowego filmu nie ma ładowania
        }
    }

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
                _comment.value = film.comment
                _posterPath.value = film.posterPath
            }
            _isLoading.value = false
        }
    }

    fun setTitle(title: String) {
        _title.value = title

        if (title.isNotBlank() && _validationErrors.value.containsKey("title")) {
            _validationErrors.value = _validationErrors.value - "title"
        }
    }

    fun setReleaseDate(date: Date) {
        _releaseDate.value = date
        if (_validationErrors.value.containsKey("releaseDate")) {
            _validationErrors.value = _validationErrors.value - "releaseDate"
        }
    }

    fun setCategory(category: String) {
        _category.value = category
        if (category.isNotBlank() && _validationErrors.value.containsKey("category")) {
            _validationErrors.value = _validationErrors.value - "category"
        }
    }

    fun setWatched(isWatched: Boolean) {
        _isWatched.value = isWatched
        // Jeśli odznaczono "obejrzane", a był błąd oceny, usuń go
        if (!isWatched && _validationErrors.value.containsKey("rating")) {
            _validationErrors.value = _validationErrors.value - "rating"
        }
        // Jeśli zaznaczono "obejrzane", ale nie ma oceny, błąd pojawi się przy walidacji
    }

    fun setRating(rating: Int?) {
        _rating.value = rating
        if (rating != null && _validationErrors.value.containsKey("rating")) {
            _validationErrors.value = _validationErrors.value - "rating"
        }
    }

    fun setComment(comment: String) {
        _comment.value = comment
    }

    fun selectPoster(uri: Uri) {
        viewModelScope.launch {
            // Można dodać _isLoading.value = true tutaj, jeśli zapis obrazu trwa
            val path = imageUtils.saveImageFromUri(uri)
            _posterPath.value = path
            // _isLoading.value = false
        }
    }

    private fun validateForm(): Boolean {
        val errors = mutableMapOf<String, Int>()

        if (_title.value.isBlank()) {
            errors["title"] = R.string.validation_error_title_empty
        }

        val calendar = Calendar.getInstance().apply { add(Calendar.YEAR, 2) }
        if (_releaseDate.value.after(calendar.time)) {
            errors["releaseDate"] = R.string.validation_error_date_too_far
        }

        if (_category.value.isBlank() || !FilmCategories.categories.contains(_category.value)) {
            errors["category"] = R.string.validation_error_category_not_selected
        }

        if (_isWatched.value && _rating.value == null) {
            errors["rating"] = R.string.validation_error_rating_required_for_watched
        } else if (_isWatched.value && _rating.value != null && (_rating.value!! < 1 || _rating.value!! > 10)) {
            // Dodatkowa walidacja zakresu oceny, jeśli jest wpisana
            errors["rating"] = R.string.add_edit_hint_rating
        }


        _validationErrors.value = errors
        return errors.isEmpty()
    }

    fun saveFilm() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true // Pokaż ładowanie podczas zapisu
            val filmToSave = Film(
                id = filmId.takeIf { it > 0 } ?: 0, // Użyj 0 dla nowego filmu, inaczej istniejące ID
                title = _title.value.trim(),
                releaseDate = _releaseDate.value,
                category = _category.value,
                isWatched = _isWatched.value,
                rating = if (_isWatched.value) _rating.value else null, // Ocena tylko jeśli obejrzane
                comment = _comment.value?.trim()?.takeIf { it.isNotBlank() },
                posterPath = _posterPath.value
            )

            if (filmToSave.id > 0) {
                repository.updateFilm(filmToSave)
            } else {
                repository.insertFilm(filmToSave)
            }
            _isLoading.value = false
            _isSaved.value = true
        }
    }

    class Factory(
        private val repository: FilmRepository,
        private val filmId: Long,
        private val imageUtils: ImageUtils,
        private val application: Application // Dodano Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FilmEditViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FilmEditViewModel(repository, filmId, imageUtils, application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
