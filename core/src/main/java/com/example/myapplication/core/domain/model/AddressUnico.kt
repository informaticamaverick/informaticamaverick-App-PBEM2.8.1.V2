package com.example.myapplication.core.domain.model

import java.util.UUID

/**
 * --- MODELO DE DIRECCIÓN ÚNICO (SSOT) ---
 * Representa una dirección física unificada para todo el ecosistema Maverick.
 * Reemplaza a AddressClient y AddressProvider para garantizar paridad de datos.
 */
data class AddressUnico(
    val id: String = UUID.randomUUID().toString(),
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val departamento: String = "",
    val localidad: String = "",
    val provincia: String = "",
    val pais: String = "Argentina",
    val codigoPostal: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val label: String = "", // Ej: "Mi Domicilio", "Sucursal Centro"

    // --- METADATOS DE CONTEXTO (Opcionales para UI/Lógica) ---
    val ownerId: String? = null,
    val ownerName: String? = null,
    val isCompany: Boolean = false
) {
    /**
     * Devuelve la dirección formateada como una sola línea de texto.
     */
    fun fullString(): String {
        val calleYNumero = listOf(calle, numero).filter { it.isNotBlank() }.joinToString(" ")
        val pisoDepto = listOf(piso, departamento).filter { it.isNotBlank() }.joinToString(" ")
        return listOf(calleYNumero, pisoDepto, localidad, provincia, pais)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }

    /**
     * Proyección para compatibilidad con UI heredada.
     */
    val streetAndNumber: String
        get() = listOf(calle, numero).filter { it.isNotBlank() }.joinToString(" ")
}
