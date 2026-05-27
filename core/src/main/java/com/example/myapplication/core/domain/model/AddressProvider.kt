package com.example.myapplication.core.domain.model

import java.util.UUID

/**
 * --- MODELO DE DIRECCIÓN PRESTADOR (PREMIUM) ---
 * Representa una dirección física asociada a un prestador, sucursal o empresa.
 * Optimizado para el sistema de geolocalización Maverick Premium.
 */
data class AddressProvider(
    val id: String = UUID.randomUUID().toString(),
    val calle: String = "",
    val numero: String = "",
    val localidad: String = "",
    val provincia: String = "",
    val pais: String = "Argentina",
    val codigoPostal: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val label: String = ""
) {
    /**
     * Devuelve la dirección formateada como una sola línea de texto.
     */
    fun fullString(): String {
        val calleYNumero = listOf(calle, numero).filter { it.isNotBlank() }.joinToString(" ")
        return listOf(calleYNumero, localidad, provincia, pais)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}

