package com.example.myapplication.prestador.data.model

import java.util.UUID

/**
 * --- MODELOS DE EMPRESA Y SUCURSALES (REPLICADO) ---
 * 
 * Estructura:
 * - CompanyProvider (Empresa)
 *   - BranchProvider (Sucursal)
 *     - EmployeeProvider (Empleado)
 *     - AddressProvider (Dirección)
 */

// --- SECCIÓN: MODELO DE EMPRESA ---
data class CompanyProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", 
    val razonSocial: String = "",
    val cuit: String = "",
    val description: String = "", 
    val rating: Float = 0f,
    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,
    val categories: List<String> = emptyList(),
    val isVerified: Boolean = false,
    
    // Una empresa puede tener varias sucursales
    val branches: List<BranchProvider> = emptyList()
)

// --- SECCIÓN: MODELO DE SUCURSAL ---
data class BranchProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", 
    val address: AddressProvider = AddressProvider(),
    val workingHours: String = "", 
    val employees: List<EmployeeProvider> = emptyList(),
    val galleryImages: List<String> = emptyList(),

    // Características de la sucursal
    val doesService: Boolean = false,
    val doesProduct: Boolean = false,
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val rating: Float = 0f
)

// --- SECCIÓN: MODELO DE EQUIPO DE TRABAJO ---
data class EmployeeProvider(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val lastName: String = "",
    val position: String = "", 
    val detail: String = "",
    val photoUrl: String? = null
)
