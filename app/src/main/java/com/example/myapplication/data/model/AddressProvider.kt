package com.example.myapplication.data.model

import java.util.UUID

/**
 * --- MODELO DE DATOS PARA DIRECCIONES (AGNOSTICO) ---
 *
 * Propósito:
 * Representa una dirección física. Este modelo es "agnóstico", lo que significa que no está
 * atado a ninguna tecnología específica (como Room o Firebase). Puede ser usado tanto por
 * la entidad `Provider` de Room como por otros modelos de datos en la aplicación.
 *
 * Características:
 * - Simplicidad: Es una `data class` simple con los campos necesarios para una dirección.
 * - Reutilizable: Puede ser embebido en múltiples entidades o modelos sin duplicar código.
 */
data class AddressProvider(
    val id: String = UUID.randomUUID().toString(),
    var calle: String = "",
    var numero: String = "",
    var localidad: String = "",
    var provincia: String = "",
    var pais: String = "",
    var codigoPostal: String = "",
    // Nuevas variables para GPS (Fast)
    var latitude: Double? = null,
    var longitude: Double? = null
) {
    fun fullString(): String {
        val calleYNumero = listOf(calle, numero).filter { it.isNotBlank() }.joinToString(" ")
        return listOf(calleYNumero, localidad, provincia, pais)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}
