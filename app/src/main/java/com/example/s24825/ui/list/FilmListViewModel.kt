package com.example.s24825.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.s24825.data.entity.Film
import com.example.s24825.data.repository.FilmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the film list screen.
 *
 * This ViewModel holds UI state and handles business logic for the film list screen,
 * including filtering, counting, and deletion operations.
 */
class FilmListViewModel(private val repository: FilmRepository) : ViewModel() {

    // Filter state flows
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _selectedWatchStatus = MutableStateFlow<Boolean?>(null)
    val selectedWatchStatus: StateFlow<Boolean?> = _selectedWatchStatus

    // Film deletion dialog state
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog

    private val _filmToDelete = MutableStateFlow<Film?>(null)

    // Combine filters with repository data to get filtered films
    val filteredFilms = combine(
        repository.allFilms,
        _selectedCategory,
        _selectedWatchStatus
    ) { films, category, watchStatus ->
        films.filter { film ->
            (category == null || film.category == category) &&
                    (watchStatus == null || film.isWatched == watchStatus)
        }
    }

    // Get the count of films matching current filters
    val itemCount: StateFlow<Int> = combine(
        _selectedCategory,
        _selectedWatchStatus
    ) { category, watchStatus ->
        Pair(category, watchStatus)
    }.flatMapLatest { (category, watchStatus) ->
        repository.getFilteredFilmsCount(category, watchStatus)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    /**
     * Sets the category filter.
     * @param category The category to filter by, or null for all categories
     */
    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    /**
     * Sets the watch status filter.
     * @param isWatched The watch status to filter by, or null for all statuses
     */
    fun setWatchStatus(isWatched: Boolean?) {
        _selectedWatchStatus.value = isWatched
    }

    /**
     * Shows the delete confirmation dialog for a film.
     * @param film The film to be deleted
     */
    fun showDeleteDialog(film: Film) {
        _filmToDelete.value = film
        _showDeleteDialog.value = true
    }

    /**
     * Dismisses the delete confirmation dialog.
     */
    fun dismissDeleteDialog() {
        _showDeleteDialog.value = false
        _filmToDelete.value = null
    }

    /**
     * Deletes the selected film and dismisses the dialog.
     */
    fun deleteFilm() {
        _filmToDelete.value?.let { film ->
            viewModelScope.launch {
                repository.deleteFilm(film)
                _showDeleteDialog.value = false
                _filmToDelete.value = null
            }
        }
    }

    /**
     * Factory for creating FilmListViewModel instances with the repository dependency.
     */
    class Factory(private val repository: FilmRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FilmListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FilmListViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}