package com.example.myapplication.core.domain.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * --- MODELO DE DOMINIO: User (PERFIL DEL CLIENTE / USUARIO) ---
 * Este objeto es el "lenguaje común" para representar al cliente en todo el ecosistema.
 * Contiene datos personales, direcciones unificadas y empresas asociadas.
 * [ELITE v5.1]: Sin flags de prestador ni categorías.
 */
@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    
    // --- DATOS PERSONALES ---
    var name: String = "",
    var lastName: String = "",
    var phoneNumber: String = "",
    var bio: String = "",
    var photoUrl: String? = null,
    val profileThumbnail: String? = null, // [LEY #3] Miniatura Base64 para carga instantánea
    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),

    // --- DIRECCIONES UNIFICADAS ---
    val personalAddresses: List<AddressUnico> = emptyList(),

    // --- EMPRESAS (Lado Cliente) ---
    @get:PropertyName("hasCompanyProfile")
    @set:PropertyName("hasCompanyProfile")
    var hasCompanyProfile: Boolean = false,
    val companies: List<CompanyClient> = emptyList(),
    
    // --- ESTADOS Y BANDERAS ---
    @get:PropertyName("isProfileComplete")
    @set:PropertyName("isProfileComplete")
    var isProfileComplete: Boolean = false,

    @get:PropertyName("isSubscribed")
    @set:PropertyName("isSubscribed")
    var isSubscribed: Boolean = false,

    @get:PropertyName("isVerified")
    @set:PropertyName("isVerified")
    var isVerified: Boolean = false,

    @get:PropertyName("isOnline")
    @set:PropertyName("isOnline")
    var isOnline: Boolean = false,

    @get:PropertyName("notificationsEnabled")
    @set:PropertyName("notificationsEnabled")
    var notificationsEnabled: Boolean = false,

    @get:PropertyName("isPublicProfile")
    @set:PropertyName("isPublicProfile")
    var isPublicProfile: Boolean = false,
    
    // --- REPUTACIÓN Y FAVORITOS ---
    val rating: Float = 0f,
    val favoriteProviderIds: List<String> = emptyList(),

    // --- GEOLOCALIZACIÓN ---
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Nombre completo calculado para mostrar en UI.
     */
    val fullName: String
        get() = if (name.isNotBlank() || lastName.isNotBlank()) "$name $lastName" else displayName
        
    /**
     * Obtiene la dirección marcada como principal.
     */
    val mainAddress: AddressUnico?
        get() = personalAddresses.firstOrNull()

    /**
     * --- MAPEO INTELIGENTE (OBSOLETO - USAR SSOT) ---
     * AddressInfo está en desuso. Se recomienda manejar AddressUnico y mappers dinámicos en la UI.
     */
    /*
    fun toAddressInfoList(): List<AddressInfo> {
        val list = mutableListOf<AddressInfo>()
        // ... (lógica comentada)
        return list
    }
    */
}
