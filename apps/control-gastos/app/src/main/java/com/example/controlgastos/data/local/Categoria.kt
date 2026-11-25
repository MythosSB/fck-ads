package com.example.controlgastos.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categorias", indices = [Index(value = ["nombre"], unique = true)])
data class Categoria(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nombre: String
)
