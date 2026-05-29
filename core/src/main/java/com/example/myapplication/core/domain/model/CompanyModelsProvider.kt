package com.example.myapplication.core.domain.model

import java.util.UUID

/**
 * --- MODELOS DE EMPRESA Y SUCURSALES (LADO PRESTADOR) ---
 * Estructura jerárquica para representar la organización de un prestador profesional.
 */

/**
 * Modelo de Empresa Profesional.
 */
data class CompanyProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // Nombre de fantasía
    val razonSocial: String = "",
    val cuit: String = "",
    val email: String = "",
    val description: String = "",
    val rating: Float = 0f,
    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,
    val profileImage: Any? = null,
    val bannerImage: Any? = null,
    val categories: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val branches: List<BranchProvider> = emptyList()
)

/**
 * Modelo de Sucursal Profesional.
 */
data class BranchProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", 
    val address: AddressProvider = AddressProvider(),
    val workingHours: String = "",
    val employees: List<EmployeeProvider> = emptyList(),
    val galleryImages: List<String> = emptyList(),
    val doesService: Boolean = false,
    val doesProduct: Boolean = false,
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val rating: Float = 0f
)

/**
 * Modelo de Empleado o Miembro del Equipo.
 */
data class EmployeeProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val lastName: String = "",
    val position: String = "", // Rol (Ej: Referente, Técnico)
    val detail: String = "",
    val photoUrl: String? = null,
    val profileImage: Any? = null
)
