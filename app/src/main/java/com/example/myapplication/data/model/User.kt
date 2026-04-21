package com.example.myapplication.data.model

import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.CompanyClient
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * --- MODELO DE DOMINIO: User (PERFIL DEL DUEÑO) ---
 *
 * Este objeto se utiliza en la capa de UI y lógica de negocio.
 * Representa la proyección de los datos del dueño de la app (cliente).
 * Se han eliminado campos de perfil profesional y campos planos redundantes.
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
    val galleryImages: List<String> = emptyList(), // Galería personal del usuario

    // --- CONTACTOS ADICIONALES ---
    val additionalEmails: List<String> = emptyList(),
    val additionalPhones: List<String> = emptyList(),

    // --- DIRECCIONES PERSONALES ---
    // Soporta múltiples direcciones (Casa, Oficina, etc.)
    val personalAddresses: List<AddressClient> = emptyList(),

    // --- GESTIÓN DE NEGOCIOS (EMPRESAS) ---
    @get:PropertyName("hasCompanyProfile")
    @set:PropertyName("hasCompanyProfile")
    var hasCompanyProfile: Boolean = false, // Habilita la sección de empresas

    val companies: List<CompanyClient> = emptyList(), // Lista de empresas con sus sucursales
    
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
    
    // --- SOCIAL Y REPUTACIÓN ---
    val rating: Float = 0f, // Ranking otorgado por prestadores
    val favoriteProviderIds: List<String> = emptyList(), // Prestadores favoritos

    // --- GEOLOCALIZACIÓN Y FECHAS ---
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    
    // --- FLAG DE CONTROL DE SINCRONIZACIÓN ---
    @get:PropertyName("isSynced")
    @set:PropertyName("isSynced")
    var isSynced: Boolean = true // Indica si los datos locales coinciden con Firebase
) {
    /**
     * Nombre completo calculado
     */
    val fullName: String
        get() = if (name.isNotBlank() || lastName.isNotBlank()) "$name $lastName" else displayName
        
    /**
     * Dirección principal
     */
    val mainAddress: AddressClient?
        get() = personalAddresses.firstOrNull()
}
