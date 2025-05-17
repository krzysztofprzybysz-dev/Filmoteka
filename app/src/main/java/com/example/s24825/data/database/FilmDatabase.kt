package com.example.s24825.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.filmoteka.util.Converters
import com.example.s24825.data.dao.FilmDao
import com.example.s24825.data.entity.Film
import com.example.s24825.data.entity.FilmCategories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(entities = [Film::class], version = 1, exportSchema = false)

@TypeConverters(Converters::class)
abstract class FilmDatabase : RoomDatabase() {

    abstract fun filmDao(): FilmDao


    companion object {

        //zapewnia, ze wszystkie wątki widzą tę samą wartość i operacje sa atomowe
        @Volatile
        private var INSTANCE: FilmDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FilmDatabase {

            //jezeli null przechodzi do bloku synchronized ktory zabezpiecza przed race condition
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FilmDatabase::class.java,
                    "film_database"
                )
                    //jezeli aktualny schemat bazy danych nie pasuje do tej z entity ROOM czysci baze danych i tworzy ja od nowa
                    .fallbackToDestructiveMigration()

                    //tworzy callback ktory wykonuje sie przy tworzeniu bazy danych
                    .addCallback(FilmDatabaseCallback(scope))
                    .build()

                INSTANCE = instance
                instance
            }
        }


        private class FilmDatabaseCallback(private val scope: CoroutineScope) : Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                INSTANCE?.let { database ->
                    //populuje baze danych przy tworzeniu bazy danych w asynchronicznie
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.filmDao())
                    }
                }
            }
        }

        private suspend fun populateDatabase(filmDao: FilmDao) {

            val calendar = Calendar.getInstance()

            calendar.set(2010, Calendar.JULY, 16)

            filmDao.insert(
                Film(
                    title = "Incepcja",
                    releaseDate = calendar.time,
                    category = FilmCategories.FILM,
                    isWatched = true,
                    rating = 9,
                    comment = "Świetny film o snach i rzeczywistości.",
                    posterPath = null
                )
            )

            calendar.set(2011, Calendar.APRIL, 17)
            filmDao.insert(
                Film(
                    title = "Gra o Tron",
                    releaseDate = calendar.time,
                    category = FilmCategories.SERIAL,
                    isWatched = true,
                    rating = 8,
                    comment = "Epicki serial fantasy oparty na książkach George'a R.R. Martina.",
                    posterPath = null
                )
            )

            calendar.set(2006, Calendar.MARCH, 5)
            filmDao.insert(
                Film(
                    title = "Planeta Ziemia",
                    releaseDate = calendar.time,
                    category = FilmCategories.DOKUMENT,
                    isWatched = false,
                    rating = null,
                    comment = null,
                    posterPath = null
                )
            )

            calendar.set(1994, Calendar.SEPTEMBER, 23)
            filmDao.insert(
                Film(
                    title = "Skazani na Shawshank",
                    releaseDate = calendar.time,
                    category = FilmCategories.FILM,
                    isWatched = true,
                    rating = 10,
                    comment = "Jeden z najlepszych filmów wszech czasów.",
                    posterPath = null
                )
            )

            calendar.set(2008, Calendar.JANUARY, 20)
            filmDao.insert(
                Film(
                    title = "Breaking Bad",
                    releaseDate = calendar.time,
                    category = FilmCategories.SERIAL,
                    isWatched = false,
                    rating = null,
                    comment = null,
                    posterPath = null
                )
            )
        }
    }
}