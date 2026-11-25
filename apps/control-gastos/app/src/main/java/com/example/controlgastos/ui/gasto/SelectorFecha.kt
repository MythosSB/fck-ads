package com.example.controlgastos.ui.gasto

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.controlgastos.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoFecha = DateTimeFormatter.ofPattern("d MMM uuuu", Locale.forLanguageTag("es-MX"))

internal fun formatearFecha(fecha: LocalDate): String = fecha.format(formatoFecha)

private fun LocalDate.milisegundosUtc(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun fechaDesdeUtc(milisegundos: Long): LocalDate =
    Instant.ofEpochMilli(milisegundos).atOffset(ZoneOffset.UTC).toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectorFecha(fecha: LocalDate, onCerrar: () -> Unit, onElegir: (LocalDate) -> Unit) {
    val hoy = LocalDate.now()
    val limiteUtc = hoy.milisegundosUtc()
    val fechasPermitidas = remember(limiteUtc) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= limiteUtc
            override fun isSelectableYear(year: Int): Boolean = year <= hoy.year
        }
    }
    val estado = rememberDatePickerState(
        initialSelectedDateMillis = fecha.coerceAtMost(hoy).milisegundosUtc(),
        yearRange = minOf(1900, fecha.year)..hoy.year,
        selectableDates = fechasPermitidas
    )
    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            val seleccion = estado.selectedDateMillis
            TextButton(
                onClick = { seleccion?.let { onElegir(fechaDesdeUtc(it)) } },
                enabled = seleccion != null && seleccion <= limiteUtc
            ) {
                Text(stringResource(R.string.aceptar))
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text(stringResource(R.string.cancelar)) }
        }
    ) {
        DatePicker(
            state = estado,
            modifier = Modifier.verticalScroll(rememberScrollState()),
            title = null,
            showModeToggle = false
        )
    }
}
