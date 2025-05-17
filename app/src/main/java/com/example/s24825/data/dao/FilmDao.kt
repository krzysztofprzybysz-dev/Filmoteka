package com.example.s24825.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.s24825.data.entity.Film
import kotlinx.coroutines.flow.Flow


@Dao
interface FilmDao {

    /// TODO: Co znaczy FLOW, KORUTYNA i SUSPEND 
    
    @Query("SELECT * FROM films ORDER BY releaseDate ASC")
    fun getAllFilms(): Flow<List<Film>>

    @Query("SELECT * FROM films WHERE category = :category ORDER BY releaseDate ASC")
    fun getFilmsByCategory(category: String): Flow<List<Film>>

    @Query("SELECT * FROM films WHERE isWatched = :isWatched ORDER BY releaseDate ASC")
    fun getFilmsByWatchStatus(isWatched: Boolean): Flow<List<Film>>

    @Query("SELECT * FROM films WHERE (:category IS NULL OR category = :category) AND (:isWatched IS NULL OR isWatched = :isWatched) ORDER BY releaseDate ASC")
    fun getFilteredFilms(category: String?, isWatched: Boolean?): Flow<List<Film>>

    @Query("SELECT COUNT(*) FROM films WHERE (:category IS NULL OR category = :category) AND (:isWatched IS NULL OR isWatched = :isWatched)")
    fun getFilteredFilmsCount(category: String?, isWatched: Boolean?): Flow<Int>

    @Query("SELECT * FROM films WHERE id = :id")
    suspend fun getFilmById(id: Long): Film?

    @Insert
    suspend fun insert(film: Film): Long

    @Update
    suspend fun update(film: Film)


    @Delete
    suspend fun delete(film: Film)
}