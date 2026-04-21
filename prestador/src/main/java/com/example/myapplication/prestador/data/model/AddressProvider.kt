package com.example.myapplication.prestador.data.model

import java.util.UUID

/**
 * --- MODELO DE DATOS PARA DIRECCIONES (REPLICADO) ---
 * 
 * Este modelo representa una dirección física de forma agnóstica.
 * Se utiliza para mantener la consistencia con la aplicación de cliente.
 */
data class AddressProvider(
    val id: String = UUID.randomUUID().toString(),
    var calle: String = "",
    var numero: String = "",
    var localidad: String = "",
    var provincia: String = "",
    var pais: String = "",
    var codigoPostal: String = "",
    // Coordenadas GPS
    var latitude: Double? = null,
    var longitude: Double? = null
) {
    /**
     * Devuelve la dirección completa formateada como String.
     */
    fun fullString(): String {
        val calleYNumero = listOf(calle, numero).filter { it.isNotBlank() }.joinToString(" ")
        return listOf(calleYNumero, localidad, provincia, pais)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}
