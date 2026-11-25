package com.example.controlgastos.ui.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.controlgastos.ControlGastosApplication
import com.example.controlgastos.data.local.GastoConCategoria
import com.example.controlgastos.data.repository.GastosRepository
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

internal sealed interface EstadoInicio {
    val mes: YearMonth
    val totalCentavos: Long? get() = null

    data class Cargando(override val mes: YearMonth) : EstadoInicio
    data class Vacio(
        override val mes: YearMonth,
        override val totalCentavos: Long
    ) : EstadoInicio

    data class ConDatos(
        override val mes: YearMonth,
        override val totalCentavos: Long,
        val gastos: List<GastoConCategoria>
    ) : EstadoInicio
}

internal class InicioViewModel(
    repositorio: GastosRepository,
    mesSeleccionado: StateFlow<YearMonth>
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val estado: StateFlow<EstadoInicio> = mesSeleccionado.flatMapLatest { mes ->
        combine(
            repositorio.obtenerGastosDelMes(mes),
            repositorio.obtenerTotalDelMes(mes)
        ) { gastos, total ->
            if (gastos.isEmpty()) EstadoInicio.Vacio(mes, total)
            else EstadoInicio.ConDatos(mes, total, gastos)
        }.onStart { emit(EstadoInicio.Cargando(mes)) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = EstadoInicio.Cargando(mesSeleccionado.value)
    )

    companion object {
        fun factory(mesSeleccionado: StateFlow<YearMonth>) = viewModelFactory {
            initializer {
                val aplicacion = this[APPLICATION_KEY] as ControlGastosApplication
                InicioViewModel(aplicacion.gastosRepository, mesSeleccionado)
            }
        }
    }
}
