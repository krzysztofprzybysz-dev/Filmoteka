package com.example.s24825

import android.app.Application
import com.example.s24825.data.database.FilmDatabase
import com.example.s24825.data.repository.FilmRepository
import com.example.s24825.util.ImageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


class FilmotekaApplication : Application() {

    //tworzymy globalny zakres dla korutyny
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy {
        FilmDatabase.getDatabase(this, applicationScope)
    }

    val repository by lazy {
        FilmRepository(database.filmDao())
    }

    val imageUtils by lazy {
        ImageUtils(this)
    }
}