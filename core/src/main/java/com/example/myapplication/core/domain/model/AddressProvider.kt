package com.example.myapplication.core.domain.model

import java.util.UUID

/**
 * --- MODELO DE DIRECCIÓN PRESTADOR ---
 * Representa una dirección física asociada a un prestador, sucursal o empresa.
 */
data class AddressProvider(
    val id: String = UUID.randomUUID().toString(),
    var calle: String = "",
    var numero: String = "",
    var localidad: String = "",
    var provincia: String = "",
    var pais: String = "",
    var codigoPostal: String = "",
    // Coordenadas para geolocalización
    var latitude: Double? = null,
    var longitude: Double? = null
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
