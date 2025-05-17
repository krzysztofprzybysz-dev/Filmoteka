package com.example.s24825.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity(tableName = "films")

//klasa danych, automatycznie generuje settery, gettery, equals, hashCode, toString
data class Film(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    val releaseDate: Date,

    val category: String,

    val isWatched: Boolean = false,

    val rating: Int? = null,

    val comment: String? = null,

    val posterPath: String? = null
)


// tworzy statyczną listę kategorii filmów

object FilmCategories {
    const val FILM = "Film"
    const val SERIAL = "Serial"
    const val DOKUMENT = "Dokument"

    val categories = listOf(FILM, SERIAL, DOKUMENT)
}