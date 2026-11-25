package com.example.controlgastos.data.local

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {
    @Query("SELECT * FROM categorias ORDER BY id ASC")
    fun obtenerTodas(): Flow<List<Categoria>>

    @Query("SELECT EXISTS(SELECT 1 FROM categorias WHERE id = :id)")
    fun existePorId(id: Long): Flow<Boolean>
}
