package com.example.controlgastos.ui.resumen

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.controlgastos.ControlGastosApplication
import com.example.controlgastos.R
import com.example.controlgastos.data.local.TotalPorCategoria
import com.example.controlgastos.data.repository.GastosRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

internal data class CategoriaResumen(
    val nombre: String,
    val totalCentavos: Long,
    val porcentajeRedondeado: Int,
    val progreso: Float
) {
    @get:StringRes
    val recursoPorcentaje: Int
        get() = if (porcentajeRedondeado == 0) R.string.resumen_porcentaje_menor_uno
        else R.string.resumen_porcentaje
}

internal sealed interface EstadoResumen {
    val mes: YearMonth
    val totalCentavos: Long? get() = null

    data class Cargando(override val mes: YearMonth) : EstadoResumen

    data class Vacio(
        override val mes: YearMonth,
        override val totalCentavos: Long
    ) : EstadoResumen

    data class ConDatos(
        override val mes: YearMonth,
        override val totalCentavos: Long,
        val categorias: List<CategoriaResumen>
    ) : EstadoResumen
}

internal fun calcularEstadoResumen(
    mes: YearMonth,
    total: Long,
    categorias: List<TotalPorCategoria>
): EstadoResumen? {
    // Room puede emitir el resultado de una consulta antes que el de la otra.
    if (categorias.sumOf { it.totalCentavos } != total) return null
    if (total == 0L) return EstadoResumen.Vacio(mes, total)

    val totalDecimal = BigDecimal.valueOf(total)
    val desglose = categorias.map { categoria ->
        val porcentaje = BigDecimal.valueOf(categoria.totalCentavos)
            .movePointRight(2)
            .divide(totalDecimal, 0, RoundingMode.HALF_UP)
            .intValueExact()
        CategoriaResumen(
            nombre = categoria.nombreCategoria,
            totalCentavos = categoria.totalCentavos,
            porcentajeRedondeado = porcentaje,
            progreso = (categoria.totalCentavos.toFloat() / total.toFloat())
                .coerceIn(0f, 1f)
        )
    }
    return EstadoResumen.ConDatos(mes, total, desglose)
}

internal class ResumenViewModel(
    repositorio: GastosRepository,
    mesSeleccionado: StateFlow<YearMonth>
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val estado: StateFlow<EstadoResumen> = mesSeleccionado.flatMapLatest { mes ->
        combine(
            repositorio.obtenerTotalDelMes(mes),
            repositorio.obtenerTotalesPorCategoriaDelMes(mes)
        ) { total, categorias ->
            calcularEstadoResumen(mes, total, categorias)
        }.filterNotNull().onStart { emit(EstadoResumen.Cargando(mes)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = EstadoResumen.Cargando(mesSeleccionado.value)
    )

    companion object {
        fun factory(mesSeleccionado: StateFlow<YearMonth>) = viewModelFactory {
            initializer {
                val aplicacion = this[APPLICATION_KEY] as ControlGastosApplication
                ResumenViewModel(aplicacion.gastosRepository, mesSeleccionado)
            }
        }
    }
}
