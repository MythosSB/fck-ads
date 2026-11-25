package com.example.controlgastos.ui.inicio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.controlgastos.R
import com.example.controlgastos.data.local.GastoConCategoria
import com.example.controlgastos.ui.gasto.formatearFecha
import com.example.controlgastos.ui.gasto.formatearPesos
import com.example.controlgastos.ui.mes.MesViewModel
import com.example.controlgastos.ui.mes.SelectorMes

@Composable
internal fun Inicio(
    mesViewModel: MesViewModel,
    inicioViewModel: InicioViewModel,
    onEditar: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val mes by mesViewModel.mesSeleccionado.collectAsStateWithLifecycle()
    val estado by inicioViewModel.estado.collectAsStateWithLifecycle()
    val estadoVisible = estado.takeIf { it.mes == mes } ?: EstadoInicio.Cargando(mes)

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
                is EstadoInicio.Cargando -> CircularProgressIndicator()
                is EstadoInicio.Vacio -> InicioVacio()
                is EstadoInicio.ConDatos -> key(mes) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(estadoVisible.gastos, key = { it.gasto.id }) { gasto ->
                            FilaGasto(gasto, onClick = { onEditar(gasto.gasto.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InicioVacio() {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            stringResource(R.string.sin_gastos_del_mes),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(R.string.indicacion_agregar_gasto),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FilaGasto(registro: GastoConCategoria, onClick: () -> Unit) {
    val gasto = registro.gasto
    ListItem(
        headlineContent = { Text(registro.nombreCategoria) },
        supportingContent = {
            Column {
                Text(formatearFecha(gasto.fecha))
                gasto.nota?.takeIf { it.isNotBlank() }?.let { nota ->
                    Text(nota, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        },
        trailingContent = {
            Text(formatearPesos(gasto.montoCentavos), style = MaterialTheme.typography.titleSmall)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
