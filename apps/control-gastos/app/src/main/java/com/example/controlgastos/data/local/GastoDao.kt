package com.example.controlgastos.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Insert
    suspend fun insertar(gasto: Gasto): Long

    @Update
    suspend fun actualizar(gasto: Gasto): Int

    @Delete
    suspend fun eliminar(gasto: Gasto): Int

    @Query("SELECT * FROM gastos WHERE id = :id")
    fun obtenerPorId(id: Long): Flow<Gasto?>

    @Query(
        """
        SELECT g.*, c.nombre AS nombreCategoria
        FROM gastos AS g
        INNER JOIN categorias AS c ON g.categoriaId = c.id
        WHERE g.fecha BETWEEN :fechaInicio AND :fechaFin
        ORDER BY g.fecha DESC, g.id DESC
        """
    )
    fun obtenerEntreFechas(
        fechaInicio: LocalDate,
        fechaFin: LocalDate
    ): Flow<List<GastoConCategoria>>

    @Query(
        """
        SELECT COALESCE(SUM(montoCentavos), 0)
        FROM gastos
        WHERE fecha BETWEEN :fechaInicio AND :fechaFin
        """
    )
    fun obtenerTotalEntreFechas(fechaInicio: LocalDate, fechaFin: LocalDate): Flow<Long>

    @Query(
        """
        SELECT c.nombre AS nombreCategoria, SUM(g.montoCentavos) AS totalCentavos
        FROM gastos AS g
        INNER JOIN categorias AS c ON g.categoriaId = c.id
        WHERE g.fecha BETWEEN :fechaInicio AND :fechaFin
        GROUP BY c.id, c.nombre
        ORDER BY totalCentavos DESC, c.id ASC
        """
    )
    fun obtenerTotalesPorCategoriaEntreFechas(
        fechaInicio: LocalDate,
        fechaFin: LocalDate
    ): Flow<List<TotalPorCategoria>>
}
