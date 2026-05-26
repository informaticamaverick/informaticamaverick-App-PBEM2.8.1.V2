package com.example.myapplication.core.domain.model

import java.util.UUID

/**
 * --- MODELO DE DIRECCIÓN CLIENTE ---
 * Representa una dirección física asociada a un usuario o una empresa del cliente.
 * Se utiliza para la geolocalización de servicios y envíos.
 */
data class AddressClient(
    val id: String = UUID.randomUUID().toString(),
    val calle: String = "",
    val numero: String = "",
    val localidad: String = "",
    val provincia: String = "",
    val pais: String = "",
    val codigoPostal: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val label: String = "" // Ej: "Casa", "Sucursal Centro"
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
