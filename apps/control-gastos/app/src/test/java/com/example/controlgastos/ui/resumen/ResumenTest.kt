package com.example.controlgastos.ui.resumen

import com.example.controlgastos.R
import com.example.controlgastos.data.local.TotalPorCategoria
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class ResumenTest {
    private val mes = YearMonth.of(2024, 2)

    @Test
    fun calculaElRepartoRedondeaYConservaElOrden() {
        val estado = calcularEstadoResumen(
            mes,
            10_000L,
            listOf(TotalPorCategoria("Comida", 5_450L), TotalPorCategoria("Hogar", 4_550L))
        ) as EstadoResumen.ConDatos

        assertEquals(10_000L, estado.totalCentavos)
        assertEquals(
            listOf(
                CategoriaResumen("Comida", 5_450L, 55, 0.545f),
                CategoriaResumen("Hogar", 4_550L, 46, 0.455f)
            ),
            estado.categorias
        )
    }

    @Test
    fun asignaCienPorCientoAUnaSolaCategoria() {
        val estado = calcularEstadoResumen(
            mes, 300L, listOf(TotalPorCategoria("Comida", 300L))
        ) as EstadoResumen.ConDatos

        val categoria = estado.categorias.single()
        assertEquals(100, categoria.porcentajeRedondeado)
        assertEquals(1f, categoria.progreso, 0f)
        assertEquals(R.string.resumen_porcentaje, categoria.recursoPorcentaje)
    }

    @Test
    fun eligeLaEtiquetaMenorAUnPorCientoSiRedondeaACero() {
        val estado = calcularEstadoResumen(
            mes, 1_000L, listOf(TotalPorCategoria("Comida", 999L), TotalPorCategoria("Ocio", 1L))
        ) as EstadoResumen.ConDatos

        val categoria = estado.categorias.last()
        assertEquals(0, categoria.porcentajeRedondeado)
        assertEquals(R.string.resumen_porcentaje_menor_uno, categoria.recursoPorcentaje)
        assertEquals(0.001f, categoria.progreso, 0.000001f)
    }

    @Test
    fun devuelveEstadoVacioCuandoElTotalEsCero() {
        assertEquals(EstadoResumen.Vacio(mes, 0L), calcularEstadoResumen(mes, 0L, emptyList()))
    }
}
