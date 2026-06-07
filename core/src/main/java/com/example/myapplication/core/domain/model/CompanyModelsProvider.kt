package com.example.myapplication.core.domain.model

import java.util.UUID

/**
 * --- MODELOS DE EMPRESA Y SUCURSALES (LADO PRESTADOR) ---
 * Estructura jerárquica para representar la organización de un prestador profesional.
 * [ELITE v5.1]: Mantiene categorías y flags de servicio para búsqueda.
 */

data class CompanyProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val razonSocial: String = "",
    val description: String = "",
    val cuit: String = "",
    val email: String = "",
    val rating: Float = 0f,
    val photoUrl: String? = null,
    val thumbnailBase64: String? = null, // [LEY #3]
    val categories: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val branches: List<BranchProvider> = emptyList()
)

data class BranchProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val workingHours: String = "", 
    val address: AddressUnico = AddressUnico(),

    // Flags de servicio (Esenciales para el buscador del Cliente)
    val doesService: Boolean = false,
    val doesProduct: Boolean = false,
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    
    val team: List<EmployeeProvider> = emptyList(),
    val rating: Float = 0f
)

data class EmployeeProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val lastName: String = "",
    val position: String = "",
    val detail: String = ""
)
