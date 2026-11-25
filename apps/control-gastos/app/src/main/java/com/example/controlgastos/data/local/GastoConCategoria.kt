package com.example.controlgastos.data.local

import androidx.room.Embedded

data class GastoConCategoria(
    @Embedded val gasto: Gasto,
    val nombreCategoria: String
)
