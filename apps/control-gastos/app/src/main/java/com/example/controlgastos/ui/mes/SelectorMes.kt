package com.example.controlgastos.ui.mes

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.controlgastos.R
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeMes = Locale.forLanguageTag("es-MX")
private val formatoMes = DateTimeFormatter.ofPattern("LLLL uuuu", localeMes)

@Composable
internal fun SelectorMes(
    mes: YearMonth,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onAnterior,
            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.mes_anterior)
            )
        }
        Text(
            text = mes.format(formatoMes).replaceFirstChar { it.titlecase(localeMes) },
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        IconButton(
            onClick = onSiguiente,
            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            enabled = mes.isBefore(YearMonth.now())
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.mes_siguiente)
            )
        }
    }
}
