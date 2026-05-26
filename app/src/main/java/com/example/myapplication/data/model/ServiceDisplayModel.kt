package com.example.myapplication.data.model

/**
 * --- MODELO UNIFICADO DE RESULTADOS ---
 * Representa la versión simplificada y unificada de cualquier entidad que ofrezca servicios 
 * (Independientes o Empresas). Facilita que la UI solo dibuje sin lógica extra.
 */
data class ServiceDisplayModel(
    val id: String,
    val title: String,              // Nombre del Prestador o de la Empresa
    val subtitle: String? = null,    // Ejem: "Independiente" o "Nombre de la Empresa"
    val email: String? = null,       // Para detalles de contacto en la UI
    val photoUrl: String,
    val rating: Double,
    val isVerified: Boolean,
    val isOnline: Boolean,
    val isFavorite: Boolean = false, 
    val type: ProviderType,         // Diferencia entre INDIVIDUAL y COMPANY
    
    // --- FLAGS DE SERVICIO UNIFICADOS ---
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesService: Boolean = false,
    val doesProduct: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val isSubscribed: Boolean = false,
    val categoryId: String? = null,
    val categories: List<String> = emptyList(), // Para el ModalBottomSheet
    val displayAddress: String? = null,         // Dirección formateada
    val branchName: String? = null,             // Nombre de sucursal si aplica
    val createdAt: Long = 0L,                   // Para ordenamiento en Chat
    
    // --- COORDENADAS PARA BÚSQUEDA GEOGRÁFICA (SISTEMA FAST) ---
    val latitude: Double? = null,               // 🔥 Agregado para el Radar Fast
    val longitude: Double? = null,               // 🔥 Agregado para el Radar Fast

    // 🔥 NUEVO: Distancia calculada respecto al usuario (en km)
    val distanceKm: Double? = null,

    // ==========================================================================================
    // ---------- SECCIÓN: CAMPOS PRE-CALCULADOS (PLAN DE ACCIÓN) -------------------------------
    // ==========================================================================================
    val typeEmoji: String = "",                 // "🏢" o "👨‍🔧" pre-calculado
    val typeLabel: String = "",                 // "Empresa Certificada" o "Profesional" pre-calculado
    val badgeList: List<BadgeDisplayData> = emptyList(), // Lista de badges lista para iterar
    val companyId: String? = null                        // ID de empresa si el prestador tiene una
)

/**
 * Representa un badge visual listo para mostrarse sin lógica de decisión en la UI.
 */
data class BadgeDisplayData(
    val id: String,
    val icon: String,
    val label: String,
    val isActive: Boolean
)

enum class ProviderType { INDIVIDUAL, COMPANY }
