package com.example.s24825.data.repository

import com.example.s24825.data.dao.FilmDao
import com.example.s24825.data.entity.Film
import kotlinx.coroutines.flow.Flow


class FilmRepository(private val filmDao: FilmDao) {

    val allFilms: Flow<List<Film>> = filmDao.getAllFilms()


    fun getFilteredFilms(category: String?, isWatched: Boolean?): Flow<List<Film>> {
        return filmDao.getFilteredFilms(category, isWatched)
    }


    fun getFilteredFilmsCount(category: String?, isWatched: Boolean?): Flow<Int> {
        return filmDao.getFilteredFilmsCount(category, isWatched)
    }

    suspend fun getFilmById(id: Long): Film? {
        return filmDao.getFilmById(id)
    }


    suspend fun insertFilm(film: Film): Long {
        return filmDao.insert(film)
    }


    suspend fun updateFilm(film: Film) {
        filmDao.update(film)
    }


    suspend fun deleteFilm(film: Film) {
        filmDao.delete(film)
    }
}