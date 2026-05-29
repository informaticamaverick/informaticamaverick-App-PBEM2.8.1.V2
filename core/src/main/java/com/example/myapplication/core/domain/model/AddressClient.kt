package com.example.myapplication.core.domain.model

import java.util.UUID

/**
 * --- MODELO DE DIRECCIÓN CLIENTE (PREMIUM) ---
 * Representa una dirección física asociada a un usuario o una empresa del cliente.
 * Optimizado para el sistema de geolocalización Maverick Premium.
 */
data class AddressClient(
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

/**
 * --- MODELO DE DATOS PARA UI (READY-TO-CONSUME) ---
 * Representa una dirección aplanada y lista para ser mostrada en listas, popups o cabeceras.
 * Unifica direcciones personales y de sucursales empresariales.
 */
data class AddressInfo(
    val id: String,
    val ownerId: String? = null, // ID del propietario (User=null o Company.id)
    val companyOrUserName: String,
    val profilePhoto: Any? = null, // Foto procesada (User.profileImage o Company.profileImage)
    val branchName: String,
    val streetAndNumber: String,
    val locality: String,
    val province: String = "",
    val country: String = "",
    val postalCode: String,
    val isCompany: Boolean,
    val lat: Double,
    val lng: Double
)
