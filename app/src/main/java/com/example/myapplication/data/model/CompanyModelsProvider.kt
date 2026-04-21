package com.example.myapplication.data.model

import java.util.UUID

/**
 * --- MODELOS DE EMPRESA Y SUCURSALES ---
 * [RESTRUCTURADO] Según nuevos requerimientos de base de datos.
 * Se divide la lógica en Empresa y Sucursales con sus respectivas variables.
 */

// --- SECCIÓN: MODELO DE EMPRESA ---
data class CompanyProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // Nombre de fantasía
    val razonSocial: String = "",
    val cuit: String = "",
    val description: String = "", // Detalle o descripción de la empresa
    val rating: Float = 0f, // Rating de la empresa
    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,
    val categories: List<String> = emptyList(),
    val isVerified: Boolean = false, // Verificación a nivel empresa
    
    // Cada empresa puede tener una o varias sucursales
    val branches: List<BranchProvider> = emptyList()
)

// --- SECCIÓN: MODELO DE SUCURSAL ---
data class BranchProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // Ej: "Sucursal Norte" o "Casa Central"
    val address: AddressProvider = AddressProvider(), // Cada sucursal tiene solo 1 dirección
    val workingHours: String = "", // Horario específico de esta sucursal
    val employees: List<EmployeeProvider> = emptyList(), // Equipo de trabajo
    val galleryImages: List<String> = emptyList(), // Galería de imágenes por sucursal

    // Variables de características de la sucursal
    val doesService: Boolean = false, // Realiza Servicio??
    val doesProduct: Boolean = false, // Vende Producto??
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val rating: Float = 0f // Calificación independiente por sucursal
)

// --- SECCIÓN: MODELO DE EQUIPO DE TRABAJO ---
data class EmployeeProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val lastName: String = "",
    val position: String = "", // Rol (Ej: Referente, Técnico)
    val detail: String = "",
    val photoUrl: String? = null
)
