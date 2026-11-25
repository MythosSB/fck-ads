package com.example.controlgastos.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

class DateConverters {
    @TypeConverter
    fun fromLocalDate(fecha: LocalDate): Long = fecha.toEpochDay()

    @TypeConverter
    fun toLocalDate(dias: Long): LocalDate = LocalDate.ofEpochDay(dias)
}
