package com.example.controlgastos.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "gastos",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("categoriaId"), Index("fecha")]
)
data class Gasto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val montoCentavos: Long,
    val categoriaId: Long,
    val fecha: LocalDate,
    val nota: String? = null
)
