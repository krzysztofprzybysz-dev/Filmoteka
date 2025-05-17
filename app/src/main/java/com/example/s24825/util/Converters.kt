package com.example.filmoteka.util

import androidx.room.TypeConverter
import java.util.Date


class Converters {


    // customowe konwertery zeby Room mogl operowac na niestanardowych typach danych
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { timestamp -> Date(timestamp) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}