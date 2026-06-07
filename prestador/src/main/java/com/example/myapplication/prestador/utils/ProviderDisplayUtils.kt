package com.example.myapplication.prestador.utils

import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.core.domain.model.Provider

// =================================================================================
// --- SECCIÓN: MODELO DE UI REAL ---
// =================================================================================

data class PrestadorProfile(
    val id: String,
    val name: String,
    val lastName: String,
    val profileImageUrl: String?,
    val rating: Float,
    val services: List<String>,
    val companyName: String?,
    val address: String,
    val email: String,
    val phone: String,
    val doesHomeVisits: Boolean,
    val hasPhysicalLocation: Boolean,
    val works24h: Boolean,
    val isFavorite: Boolean,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val isSubscribed: Boolean
)

// =================================================================================
// --- SECCIÓN: UTILIDADES DE DISPLAY (TEXTO) ---
// =================================================================================

/**
 * Obtiene el nombre a mostrar (Empresa o Nombre Completo) según la configuración.
 * [SSOT]: Usa priorizarEmpresa del núcleo core.
 */
fun Provider.displayCompanyOrFullName(company: CompanyProvider? = null): String {
    val selectedCompany = company ?: companies.firstOrNull()
    val companyName = selectedCompany?.name?.takeIf { it.isNotBlank() }

    if (priorizarEmpresa && companyName != null) return companyName

    val fullName = (name + " " + lastName).trim()
    return fullName.ifBlank { "Prestador" }
}

/**
 * Obtiene la dirección a mostrar según la configuración.
 * [SSOT]: Usa AddressUnico y jerarquía de sucursales.
 */
fun Provider.displayAddress(company: CompanyProvider? = null): String {
    val selectedCompany = company ?: companies.firstOrNull()
    
    return when {
        priorizarEmpresa && selectedCompany != null -> {
            // Si prioriza empresa, buscamos la dirección de la primera sucursal o de la empresa
            selectedCompany.branches.firstOrNull()?.address?.fullString() 
                ?: address?.fullString() 
                ?: ""
        }
        else -> address?.fullString() ?: ""
    }
}

// =================================================================================
// --- SECCIÓN: CONVERSOR A MODELO DE UI REAL ---
// =================================================================================

/**
 * Convierte Provider (modelo de dominio Core) al modelo de visualización oficial.
 * [LEY #1]: Pantallas tontas consumen este modelo simplificado.
 */
fun Provider.toPrestadorProfile(company: CompanyProvider? = null): PrestadorProfile {
    val selectedCompany = company ?: companies.firstOrNull()
    val companyName = selectedCompany?.name?.takeIf { it.isNotBlank() }

    return PrestadorProfile(
        id = uid,
        name = name.ifBlank { "Prestador" },
        lastName = lastName,
        profileImageUrl = photoUrl,
        rating = rating,
        services = categories,
        companyName = companyName,
        address = displayAddress(company),
        email = email,
        phone = phoneNumber,
        doesHomeVisits = doesHomeVisits,
        hasPhysicalLocation = hasPhysicalLocation || (priorizarEmpresa && selectedCompany != null),
        works24h = works24h,
        isFavorite = isFavorite,
        isVerified = isVerified,
        isOnline = isOnline,
        isSubscribed = isSubscribed
    )
}
