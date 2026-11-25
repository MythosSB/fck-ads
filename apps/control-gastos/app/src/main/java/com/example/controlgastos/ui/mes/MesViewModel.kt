package com.example.controlgastos.ui.mes

import androidx.lifecycle.ViewModel
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class MesViewModel : ViewModel() {
    private val _mesSeleccionado = MutableStateFlow(YearMonth.now())
    val mesSeleccionado = _mesSeleccionado.asStateFlow()

    fun mesAnterior() {
        _mesSeleccionado.update { it.minusMonths(1) }
    }

    fun mesSiguiente() {
        _mesSeleccionado.update { mes ->
            if (mes.isBefore(YearMonth.now())) mes.plusMonths(1) else mes
        }
    }
}
