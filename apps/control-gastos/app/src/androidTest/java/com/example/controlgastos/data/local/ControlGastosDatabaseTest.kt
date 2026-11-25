package com.example.controlgastos.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.controlgastos.data.repository.GastosRepository
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ControlGastosDatabaseTest {
    private val febrero = YearMonth.of(2024, 2)
    private val primerDia = febrero.atDay(1)
    private val ultimoDia = febrero.atEndOfMonth()
    private lateinit var contexto: Context
    private lateinit var base: ControlGastosDatabase

    @Before
    fun abrirBase() {
        contexto = ApplicationProvider.getApplicationContext()
        base = Room.inMemoryDatabaseBuilder(contexto, ControlGastosDatabase::class.java).build()
    }

    @After
    fun cerrarBase() {
        base.close()
    }

    @Test
    fun totalIncluyeAmbosExtremosYExcluyeLosDiasFueraDelRango() = runBlocking {
        val comida = insertarCategoria("Comida")
        insertarGasto(comida, 400L, primerDia.minusDays(1))
        insertarGasto(comida, 100L, primerDia)
        insertarGasto(comida, 150L, ultimoDia)
        insertarGasto(comida, 500L, ultimoDia.plusDays(1))

        assertEquals(250L, base.gastoDao().obtenerTotalEntreFechas(primerDia, ultimoDia).first())
    }

    @Test
    fun totalSinGastosEnElRangoEsCero() = runBlocking {
        insertarGasto(insertarCategoria("Comida"), 100L, primerDia.minusDays(1))

        assertEquals(0L, base.gastoDao().obtenerTotalEntreFechas(primerDia, ultimoDia).first())
    }

    @Test
    fun totalesPorCategoriaExcluyenLasQueNoTienenGastosYSeOrdenanPorTotal() = runBlocking {
        val comida = insertarCategoria("Comida")
        val hogar = insertarCategoria("Hogar")
        val ocio = insertarCategoria("Ocio")
        insertarCategoria("Salud")
        insertarGasto(comida, 125L, primerDia)
        insertarGasto(hogar, 150L, primerDia)
        insertarGasto(hogar, 100L, ultimoDia)
        insertarGasto(ocio, 500L, ultimoDia.plusDays(1))

        assertEquals(
            listOf(TotalPorCategoria("Hogar", 250L), TotalPorCategoria("Comida", 125L)),
            base.gastoDao().obtenerTotalesPorCategoriaEntreFechas(primerDia, ultimoDia).first()
        )
    }

    @Test
    fun listaOrdenaPorFechaYEnEmpatePorIdDescendente() = runBlocking {
        val comida = insertarCategoria("Comida")
        val reciente = insertarGasto(comida, 100L, ultimoDia)
        val anterior = insertarGasto(comida, 50L, primerDia)
        val mismoDia = insertarGasto(comida, 75L, ultimoDia)

        val ids = base.gastoDao().obtenerEntreFechas(primerDia, ultimoDia).first().map { it.gasto.id }

        assertEquals(listOf(mismoDia, reciente, anterior), ids)
    }

    @Test
    fun ultimoDiaDelMesSoloApareceEnSuMes() = runBlocking {
        val repositorio = GastosRepository(base.categoriaDao(), base.gastoDao(), contexto.resources)
        val comida = insertarCategoria("Comida")
        val idFebrero = insertarGasto(comida, 150L, ultimoDia)
        val idMarzo = insertarGasto(comida, 250L, ultimoDia.plusDays(1))
        val marzo = febrero.plusMonths(1)

        assertEquals(idFebrero, repositorio.obtenerGastosDelMes(febrero).first().single().gasto.id)
        assertEquals(idMarzo, repositorio.obtenerGastosDelMes(marzo).first().single().gasto.id)
        assertEquals(150L, repositorio.obtenerTotalDelMes(febrero).first())
        assertEquals(250L, repositorio.obtenerTotalDelMes(marzo).first())
    }

    @Test
    fun claveForaneaImpideEliminarCategoriaConGastosYPermiteEliminarUnaLibre() = runBlocking {
        val conGastos = insertarCategoria("Comida")
        val libre = insertarCategoria("Ocio")
        val gasto = insertarGasto(conGastos, 100L, primerDia)
        val sqlite = base.openHelper.writableDatabase

        assertThrows(SQLiteConstraintException::class.java) {
            sqlite.execSQL("DELETE FROM categorias WHERE id = ?", arrayOf(conGastos))
        }
        sqlite.execSQL("DELETE FROM categorias WHERE id = ?", arrayOf(libre))

        assertEquals(listOf(conGastos), base.categoriaDao().obtenerTodas().first().map { it.id })
        assertEquals(conGastos, base.gastoDao().obtenerPorId(gasto).first()?.categoriaId)
    }

    @Test
    fun cargaInicialSiembraExactamenteSeisCategoriasEnOrden() = runBlocking {
        assertTrue(base.categoriaDao().obtenerTodas().first().isEmpty())
        val baseConCarga = Room.inMemoryDatabaseBuilder(contexto, ControlGastosDatabase::class.java)
            .addCallback(ControlGastosDatabase.DatabaseCallback())
            .build()
        try {
            val nombres = baseConCarga.categoriaDao().obtenerTodas().first().map { it.nombre }
            assertEquals(listOf("Comida", "Transporte", "Hogar", "Ocio", "Salud", "Otros"), nombres)
        } finally {
            baseConCarga.close()
        }
    }

    private fun insertarCategoria(nombre: String): Long =
        base.openHelper.writableDatabase.compileStatement("INSERT INTO categorias (nombre) VALUES (?)")
            .use { sentencia ->
                sentencia.bindString(1, nombre)
                sentencia.executeInsert()
            }

    private suspend fun insertarGasto(categoria: Long, monto: Long, fecha: LocalDate): Long =
        base.gastoDao().insertar(Gasto(montoCentavos = monto, categoriaId = categoria, fecha = fecha))
}
