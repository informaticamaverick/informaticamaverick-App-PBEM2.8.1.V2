package com.example.myapplication.prestador.utils

import com.example.myapplication.prestador.data.local.entity.BusinessEntity
import com.example.myapplication.prestador.data.local.entity.DireccionEntity
import com.example.myapplication.prestador.data.local.entity.ProviderEntity

// =================================================================================
// --- SECCIÓN: MODELO DE UI REAL (REEMPLAZO DE PPrestadorProfileFalso) ---
// =================================================================================

data class PrestadorProfile(
    val id: String,
    val name: String,
    val lastName: String,
    val profileImageUrl: String?,
    val bannerImageUrl: String?,
    val rating: Float,
    val services: List<String>,
    val companyName: String?,
    val address: String,
    val email: String,
    val phone: String,
    val doesHomeVisits: Boolean,
    val hasPhysicalLocation: Boolean,
    val works24h: Boolean,
    val galleryImages: List<String>,
    val isFavorite: Boolean,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val isSubscribed: Boolean
)

// =================================================================================
// --- SECCIÓN: UTILIDADES DE FORMATEO (DIRECCIONES) ---
// =================================================================================

fun DireccionEntity.formatInline(): String {
    val calleNumero = listOfNotNull(calle?.takeIf { it.isNotBlank() }, numero?.takeIf { it.isNotBlank() })
        .joinToString(" ")
        .trim()
    val locProv = listOfNotNull(localidad?.takeIf { it.isNotBlank() }, provincia?.takeIf { it.isNotBlank() })
        .joinToString(", ")
        .trim()
    val cp = codigoPostal?.takeIf { it.isNotBlank() }

    return listOfNotNull(
        calleNumero.takeIf { it.isNotBlank() },
        locProv.takeIf { it.isNotBlank() },
        cp
    ).joinToString(" • ").trim()
}

// =================================================================================
// --- SECCIÓN: UTILIDADES DE DISPLAY (TEXTO) ---
// =================================================================================

fun ProviderEntity.displayCompanyOrFullName(business: BusinessEntity? = null): String {
    val company = nombreEmpresa?.takeIf { it.isNotBlank() }
        ?: business?.nombreNegocio?.takeIf { it.isNotBlank() }

    if (tieneEmpresa && company != null) return company

    val fullName = (name + " " + apellido).trim()
    return fullName.ifBlank { "Prestador" }
}

fun ProviderEntity.displayAddress(business: BusinessEntity? = null): String {
    return when {
        tieneEmpresa && business != null && business.direccion.isNotBlank() -> business.direccion
        tieneEmpresa && !direccionEmpresa.isNullOrBlank() -> direccionEmpresa!!
        turnosEnLocal && !direccionLocal.isNullOrBlank() -> direccionLocal!!
        address != null -> address!!.fullString()
        else -> ""
    }
}

// =================================================================================
// --- SECCIÓN: CONVERSOR A MODELO DE UI REAL ---
// =================================================================================

/**
 * Convierte ProviderEntity (datos reales) al modelo de visualización oficial.
 */
fun ProviderEntity.toPrestadorProfile(business: BusinessEntity? = null): PrestadorProfile {
    val company = nombreEmpresa?.takeIf { it.isNotBlank() }
        ?: business?.nombreNegocio?.takeIf { it.isNotBlank() }

    return PrestadorProfile(
        id = id,
        name = name.ifBlank { "Prestador" },
        lastName = apellido,
        profileImageUrl = imageUrl,
        bannerImageUrl = bannerImageUrl,
        rating = rating,
        services = categories,
        companyName = company,
        address = displayAddress(business),
        email = email,
        phone = phoneNumber,
        doesHomeVisits = vaDomicilio,
        hasPhysicalLocation = turnosEnLocal || (tieneEmpresa && (!direccionEmpresa.isNullOrBlank() || (business != null && business.direccion.isNotBlank()))),
        works24h = atencionUrgencias,
        galleryImages = galleryImages,
        isFavorite = favorito,
        isVerified = verificado,
        isOnline = isOnline,
        isSubscribed = suscripto
    )
}
