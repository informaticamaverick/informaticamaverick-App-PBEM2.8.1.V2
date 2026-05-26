package com.example.myapplication.core.domain.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * --- MODELO DE DOMINIO: User (PERFIL DEL CLIENTE / USUARIO) ---
 * Este objeto es el "lenguaje común" para representar al cliente en todo el ecosistema.
 * Contiene datos personales, direcciones y empresas asociadas.
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
    var bannerImageUrl: String? = null,
    val galleryImages: List<String> = emptyList(),

    // --- CONTACTOS ADICIONALES ---
    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),

    // --- DIRECCIONES ---
    val personalAddresses: List<AddressClient> = emptyList(),

    // --- EMPRESAS ---
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
    
    // --- REPUTACIÓN ---
    val rating: Float = 0f,
    val favoriteProviderIds: List<String> = emptyList(),

    // --- GEOLOCALIZACIÓN ---
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    
    @get:PropertyName("isSynced")
    @set:PropertyName("isSynced")
    var isSynced: Boolean = true
) {
    /**
     * Nombre completo calculado para mostrar en UI.
     */
    val fullName: String
        get() = if (name.isNotBlank() || lastName.isNotBlank()) "$name $lastName" else displayName
        
    /**
     * Obtiene la dirección marcada como principal.
     */
    val mainAddress: AddressClient?
        get() = personalAddresses.firstOrNull()
}
