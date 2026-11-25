package com.example.controlgastos.ui.resumen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.controlgastos.R
import com.example.controlgastos.ui.gasto.formatearPesos
import com.example.controlgastos.ui.mes.MesViewModel
import com.example.controlgastos.ui.mes.SelectorMes

@Composable
internal fun Resumen(
    mesViewModel: MesViewModel,
    resumenViewModel: ResumenViewModel,
    modifier: Modifier = Modifier
) {
    val mes by mesViewModel.mesSeleccionado.collectAsStateWithLifecycle()
    val estado by resumenViewModel.estado.collectAsStateWithLifecycle()
    val estadoVisible = estado.takeIf { it.mes == mes } ?: EstadoResumen.Cargando(mes)

    Column(modifier) {
        SelectorMes(
            mes = mes,
            onAnterior = mesViewModel::mesAnterior,
            onSiguiente = mesViewModel::mesSiguiente,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 12.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.total_del_mes), style = MaterialTheme.typography.titleMedium)
                estadoVisible.totalCentavos?.let { total ->
                    Text(formatearPesos(total), style = MaterialTheme.typography.headlineMedium)
                }
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when (estadoVisible) {
                is EstadoResumen.Cargando -> CircularProgressIndicator()
                is EstadoResumen.Vacio -> Text(
                    text = stringResource(R.string.sin_gastos_del_mes),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                is EstadoResumen.ConDatos -> key(mes) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(estadoVisible.categorias, key = { it.nombre }) { categoria ->
                            FilaCategoria(categoria)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaCategoria(categoria: CategoriaResumen) {
    val redondeado = categoria.porcentajeRedondeado
    val recursoPorcentaje = categoria.recursoPorcentaje
    val menorAUno = recursoPorcentaje == R.string.resumen_porcentaje_menor_uno
    val porcentaje = if (menorAUno) {
        stringResource(recursoPorcentaje)
    } else {
        stringResource(recursoPorcentaje, redondeado)
    }
    val descripcion = if (menorAUno) {
        stringResource(R.string.resumen_barra_menor_uno, categoria.nombre)
    } else {
        stringResource(R.string.resumen_barra_porcentaje, categoria.nombre, redondeado)
    }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoria.nombre,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(
                    R.string.resumen_monto_porcentaje,
                    formatearPesos(categoria.totalCentavos),
                    porcentaje
                ),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End
            )
        }
        LinearProgressIndicator(
            progress = { categoria.progreso },
            modifier = Modifier.fillMaxWidth().clearAndSetSemantics {
                contentDescription = descripcion
            }
        )
    }
}
