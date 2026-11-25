package com.example.controlgastos.data.repository

import android.content.res.Resources
import androidx.annotation.StringRes
import com.example.controlgastos.R
import com.example.controlgastos.data.local.Categoria
import com.example.controlgastos.data.local.CategoriaDao
import com.example.controlgastos.data.local.Gasto
import com.example.controlgastos.data.local.GastoConCategoria
import com.example.controlgastos.data.local.GastoDao
import com.example.controlgastos.data.local.TotalPorCategoria
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal fun interface MensajesValidacionGasto {
    fun obtener(@StringRes id: Int, vararg argumentos: Any): String
}

class GastosRepository internal constructor(
    private val categoriaDao: CategoriaDao,
    private val gastoDao: GastoDao,
    private val mensajes: MensajesValidacionGasto
) {
    constructor(categoriaDao: CategoriaDao, gastoDao: GastoDao, recursos: Resources) :
        this(categoriaDao, gastoDao, MensajesValidacionGasto { id, argumentos ->
            recursos.getString(id, *argumentos)
        })

    fun obtenerCategorias(): Flow<List<Categoria>> = categoriaDao.obtenerTodas()

    fun obtenerGastoPorId(id: Long): Flow<Gasto?> = gastoDao.obtenerPorId(id)

    fun obtenerGastosDelMes(mes: YearMonth): Flow<List<GastoConCategoria>> =
        gastoDao.obtenerEntreFechas(mes.atDay(1), mes.atEndOfMonth())

    fun obtenerTotalDelMes(mes: YearMonth): Flow<Long> =
        gastoDao.obtenerTotalEntreFechas(mes.atDay(1), mes.atEndOfMonth())

    fun obtenerTotalesPorCategoriaDelMes(mes: YearMonth): Flow<List<TotalPorCategoria>> =
        gastoDao.obtenerTotalesPorCategoriaEntreFechas(mes.atDay(1), mes.atEndOfMonth())

    suspend fun guardarGasto(gasto: Gasto): Long =
        gastoDao.insertar(validarYNormalizar(gasto).copy(id = 0L))

    suspend fun actualizarGasto(gasto: Gasto): Int =
        gastoDao.actualizar(validarYNormalizar(gasto))

    suspend fun eliminarGasto(gasto: Gasto): Int = gastoDao.eliminar(gasto)

    private suspend fun validarYNormalizar(gasto: Gasto): Gasto {
        require(gasto.montoCentavos in 1L..MONTO_MAXIMO_CENTAVOS) {
            mensajes.obtener(R.string.validacion_monto, MONTO_MAXIMO_CENTAVOS.toString())
        }
        val nota = gasto.nota?.trim()?.takeIf { it.isNotEmpty() }
        require(nota == null || nota.length <= NOTA_MAXIMA_CARACTERES) {
            mensajes.obtener(R.string.validacion_nota, NOTA_MAXIMA_CARACTERES.toString())
        }
        require(!gasto.fecha.isAfter(LocalDate.now())) {
            mensajes.obtener(R.string.validacion_fecha)
        }
        require(categoriaDao.existePorId(gasto.categoriaId).first()) {
            mensajes.obtener(R.string.validacion_categoria)
        }
        return gasto.copy(nota = nota)
    }

    private companion object {
        const val MONTO_MAXIMO_CENTAVOS = 999_999_999L
        const val NOTA_MAXIMA_CARACTERES = 200
    }
}
