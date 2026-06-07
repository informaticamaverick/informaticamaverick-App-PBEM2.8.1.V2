package com.example.myapplication.data.model

import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.core.domain.model.CompanyProvider

/**
 * --- MODELO UNIFICADO DE VISUALIZACIÓN ---
 * Representa los datos del prestador listos para la UI.
 * DERIVA directamente de Provider (Core) sin agregar campos de persistencia extra.
 */
data class ProviderDisplayModel(
    val id: String = "",
    val providerId: String = "", 
    val companyId: String? = null,
    val branchId: String? = null,
    
    // Identidad
    val title: String = "",
    val subtitle: String? = null,
    val photoUrl: Any? = null,
    val rating: Double = 0.0,
    
    // Datos de Contacto (Desde Domain)
    val email: String = "",
    val emails: List<String> = emptyList(),
    val phoneNumber: String = "",
    
    // Perfil Detallado (Desde Domain)
    val name: String = "",
    val lastName: String = "",
    val matricula: String? = null,
    val profesion: String? = null,
    val cuilCuit: String? = null,
    val description: String = "",
    val categories: List<String> = emptyList(),
    val workingHours: String = "",
    
    // Ubicaciones y Empresas (Desde Domain)
    val addresses: List<AddressUnico> = emptyList(),
    val companies: List<CompanyProvider> = emptyList(),
    
    // Estados y Capacidades
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val isFavorite: Boolean = false,
    val isSubscribed: Boolean = false,
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val trabajaConOtros: Boolean = false,
    val type: ProviderType = ProviderType.INDIVIDUAL,

    // --- COORDENADAS PARA BÚSQUEDA GEOGRÁFICA (SISTEMA FAST) ---
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Campos Pre-Calculados para UI (No persisten)
    val distanceKm: Double? = null,
    val nearbyBranchesCount: Int = 0, // [ELITE] Conteo para Ley Pareja
    val branchName: String? = null,
    val displayAddress: String? = null,
    val fullAddress: String? = null, // [ELITE] Dirección completa desde Index
    val typeEmoji: String = "",
    val typeLabel: String = "",
    val badgeList: List<BadgeDisplayData> = emptyList(),
    val statusText: String = "",
    val statusColor: Long = 0xFF9E9E9E
)

data class BadgeDisplayData(
    val id: String,
    val icon: String,
    val label: String,
    val isActive: Boolean
)

enum class ProviderType { INDIVIDUAL, COMPANY }
