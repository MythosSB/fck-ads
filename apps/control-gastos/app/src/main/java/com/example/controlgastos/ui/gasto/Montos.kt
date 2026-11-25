package com.example.controlgastos.ui.gasto

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private const val MAXIMO_CENTAVOS = 999_999_999L
private val patronMonto = Regex("[0-9]{0,7}(?:\\.[0-9]{0,2})?")

internal fun formatearPesos(centavos: Long): String =
    DecimalFormat("'$'#,##0.00", DecimalFormatSymbols.getInstance(Locale.US))
        .format(BigDecimal.valueOf(centavos, 2))

internal fun centavosParaEditar(centavos: Long): String =
    BigDecimal.valueOf(centavos, 2).toPlainString()

internal fun normalizarEntradaMonto(texto: String): String? =
    texto.replace(',', '.').takeIf(patronMonto::matches)

internal fun textoACentavos(texto: String): Long? {
    val decimal = normalizarEntradaMonto(texto)?.toBigDecimalOrNull() ?: return null
    return decimal.movePointRight(2).longValueExact().takeIf { it in 1L..MAXIMO_CENTAVOS }
}
