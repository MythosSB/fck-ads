package com.example.controlgastos.data.repository

import com.example.controlgastos.R
import com.example.controlgastos.data.local.Categoria
import com.example.controlgastos.data.local.CategoriaDao
import com.example.controlgastos.data.local.Gasto
import com.example.controlgastos.data.local.GastoConCategoria
import com.example.controlgastos.data.local.GastoDao
import com.example.controlgastos.data.local.TotalPorCategoria
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GastosRepositoryTest {
    private val gastos = GastoDaoFalso()
    private val repositorio = GastosRepository(CategoriaDaoFalso(), gastos) { id, _ -> id.toString() }
    private val gastoValido = Gasto(
        id = 7L,
        montoCentavos = 1_250L,
        categoriaId = 1L,
        fecha = LocalDate.of(2024, 2, 29)
    )

    @Test
    fun rechazaMontoCeroONegativo() = runTest {
        for (monto in listOf(0L, -1L)) {
            verificarRechazo(gastoValido.copy(montoCentavos = monto), R.string.validacion_monto)
        }
    }

    @Test
    fun rechazaMontoMayorAlMaximo() = runTest {
        verificarRechazo(gastoValido.copy(montoCentavos = 1_000_000_000L), R.string.validacion_monto)
    }

    @Test
    fun rechazaNotaDe201Caracteres() = runTest {
        verificarRechazo(gastoValido.copy(nota = "a".repeat(201)), R.string.validacion_nota)
    }

    @Test
    fun guardaYActualizaUnaNotaDeEspaciosComoNull() = runTest {
        val gasto = gastoValido.copy(nota = "   \t ")

        repositorio.guardarGasto(gasto)
        repositorio.actualizarGasto(gasto)

        assertEquals(
            listOf(gastoValido.copy(id = 0L, nota = null), gastoValido.copy(nota = null)),
            gastos.escrituras
        )
    }

    @Test
    fun rechazaFechaFutura() = runTest {
        val fecha = LocalDate.now().plusDays(2)
        verificarRechazo(gastoValido.copy(fecha = fecha), R.string.validacion_fecha)
    }

    @Test
    fun rechazaCategoriaInexistente() = runTest {
        verificarRechazo(gastoValido.copy(categoriaId = 99L), R.string.validacion_categoria)
    }

    private suspend fun verificarRechazo(gasto: Gasto, recurso: Int) {
        val errorGuardar = runCatching { repositorio.guardarGasto(gasto) }.exceptionOrNull()
        val errorActualizar = runCatching { repositorio.actualizarGasto(gasto) }.exceptionOrNull()

        for (error in listOf(errorGuardar, errorActualizar)) {
            assertTrue("Se esperaba IllegalArgumentException, se obtuvo $error", error is IllegalArgumentException)
            assertEquals(recurso.toString(), error?.message)
        }
        assertTrue(gastos.escrituras.isEmpty())
    }

    private class CategoriaDaoFalso : CategoriaDao {
        override fun obtenerTodas(): Flow<List<Categoria>> = flowOf(listOf(Categoria(1L, "Comida")))
        override fun existePorId(id: Long): Flow<Boolean> = flowOf(id == 1L)
    }

    private class GastoDaoFalso : GastoDao {
        val escrituras = mutableListOf<Gasto>()

        override suspend fun insertar(gasto: Gasto): Long {
            escrituras.add(gasto)
            return 1L
        }

        override suspend fun actualizar(gasto: Gasto): Int {
            escrituras.add(gasto)
            return 1
        }

        override suspend fun eliminar(gasto: Gasto): Int = error("Operación inesperada")
        override fun obtenerPorId(id: Long): Flow<Gasto?> = error("Consulta inesperada")

        override fun obtenerEntreFechas(
            fechaInicio: LocalDate,
            fechaFin: LocalDate
        ): Flow<List<GastoConCategoria>> = error("Consulta inesperada")

        override fun obtenerTotalEntreFechas(
            fechaInicio: LocalDate,
            fechaFin: LocalDate
        ): Flow<Long> = error("Consulta inesperada")

        override fun obtenerTotalesPorCategoriaEntreFechas(
            fechaInicio: LocalDate,
            fechaFin: LocalDate
        ): Flow<List<TotalPorCategoria>> = error("Consulta inesperada")
    }
}
