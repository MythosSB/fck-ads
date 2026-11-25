package com.example.controlgastos.ui.gasto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MontosTest {
    @Test
    fun convierteMontosValidosACentavos() {
        val casos = mapOf(
            "12.50" to 1_250L,
            "12,5" to 1_250L,
            "0.01" to 1L,
            "9999999.99" to 999_999_999L
        )
        for ((texto, esperado) in casos) {
            assertEquals(texto, esperado, textoACentavos(texto))
        }
    }

    @Test
    fun rechazaMontosInvalidos() {
        for (texto in listOf("10000000", "12.345", "", ".", "abc", "0")) {
            assertNull("Entrada: «$texto»", textoACentavos(texto))
        }
    }

    @Test
    fun formateaCentavosComoPesos() {
        val casos = mapOf(
            0L to "$0.00",
            5L to "$0.05",
            125_050L to "$1,250.50",
            999_999_999L to "$9,999,999.99"
        )
        for ((centavos, esperado) in casos) {
            assertEquals(esperado, formatearPesos(centavos))
        }
    }
}
