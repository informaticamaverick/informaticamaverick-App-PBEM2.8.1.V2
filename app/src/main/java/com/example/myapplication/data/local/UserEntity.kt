package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.AddressClient
import com.example.myapplication.data.model.CompanyClient
import com.example.myapplication.data.model.User

/**
 * --- ÚNICA FUENTE DE VERDAD: UserEntity (PERFIL DEL DUEÑO / CLIENTE) ---
 *
 * Esta entidad gestiona la persistencia local en Room para el usuario principal de la app.
 * Se ha reestructurado para ser eficiente, eliminando campos de prestador y datos redundantes.
 * 
 * Estructura de Datos:
 * 1. Datos Personales Básicos.
 * 2. Multimedia (Fotos, Banner, Galería).
 * 3. Contactos y Direcciones (Listas dinámicas).
 * 4. Gestión de Empresas (Sucursales, Equipos, Galerías).
 * 5. Estados y Banderas de la cuenta.
 */
@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: String = "",
    val email: String = "",
    val name: String = "",
    val lastName: String = "",
    val displayName: String = "", // Nombre que se muestra en la UI
    val phoneNumber: String = "",
    
    // --- DATOS PERSONALES Y MULTIMEDIA ---
    val bio: String = "",                // Biografía o detalle del dueño
    val photoUrl: String? = null,         // URL de la foto de perfil
    val bannerImageUrl: String? = null,   // URL del banner de perfil
    val galleryImages: List<String> = emptyList(), // Galería de imágenes personal del usuario

    // --- CONTACTOS ADICIONALES ---
    val additionalEmails: List<String> = emptyList(), // Emails secundarios
    val additionalPhones: List<String> = emptyList(), // Teléfonos secundarios

    // --- UBICACIONES PERSONALES ---
    // Lista de una o más direcciones configuradas por el usuario (Casa, Oficina, etc.)
    val personalAddresses: List<AddressClient> = emptyList(),

    // --- GESTIÓN DE NEGOCIOS (EMPRESAS) ---
    val hasCompanyProfile: Boolean = false, // Bandera para saber si el usuario habilitó el perfil de empresa
    val companies: List<CompanyClient> = emptyList(), // Lista de empresas con sus sucursales y equipos

    // --- ESTADOS DE PERFIL Y BANDERAS ---
    val isOnline: Boolean = false,          // Estado de conexión
    val isSubscribed: Boolean = false,      // Si tiene una suscripción activa
    val isVerified: Boolean = false,        // Si es una cuenta verificada
    val notificationsEnabled: Boolean = false, // Configuración de notificaciones
    val isPublicProfile: Boolean = false,    // Privacidad del perfil
    val isProfileComplete: Boolean = false, // Para saber si ya llenó los datos obligatorios
    
    // --- SOCIAL Y REPUTACIÓN ---
    val rating: Float = 0f, // Ranking/Calificación otorgada por los prestadores
    val favoriteProviderIds: List<String> = emptyList(), // Lista de IDs de prestadores marcados como favoritos

    // --- GEOLOCALIZACIÓN Y TIEMPO ---
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    
    // --- FLAG DE CONTROL DE SINCRONIZACIÓN ---
    val isSynced: Boolean = true // Indica si los datos locales coinciden con Firebase
) {
    /**
     * Propiedad calculada para obtener el nombre completo o el apodo si no hay datos.
     */
    val fullName: String
        get() = if (name.isNotBlank() || lastName.isNotBlank()) "$name $lastName" else displayName

    /**
     * Retorna la dirección marcada como principal (la primera de la lista).
     */
    val mainAddress: AddressClient?
        get() = personalAddresses.firstOrNull()

    /**
     * Mapeo de la Entidad de Room al Modelo de Dominio de la App.
     */
    fun toDomain(): User {
        return User(
            uid = id,
            email = email,
            displayName = displayName,
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
            bio = bio,
            photoUrl = photoUrl,
            bannerImageUrl = bannerImageUrl,
            galleryImages = galleryImages,
            additionalEmails = additionalEmails,
            additionalPhones = additionalPhones,
            personalAddresses = personalAddresses,
            hasCompanyProfile = hasCompanyProfile,
            companies = companies,
            isOnline = isOnline,
            isSubscribed = isSubscribed,
            isVerified = isVerified,
            notificationsEnabled = notificationsEnabled,
            isPublicProfile = isPublicProfile,
            isProfileComplete = isProfileComplete,
            rating = rating,
            favoriteProviderIds = favoriteProviderIds,
            latitude = latitude,
            longitude = longitude,
            createdAt = createdAt,
            isSynced = isSynced
        )
    }
}

/**
 * Función de extensión para convertir el modelo de dominio User de vuelta a la entidad UserEntity.
 */
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = uid,
        email = email,
        displayName = displayName,
        name = name,
        lastName = lastName,
        phoneNumber = phoneNumber,
        bio = bio,
        photoUrl = photoUrl,
        bannerImageUrl = bannerImageUrl,
        galleryImages = galleryImages,
        additionalEmails = additionalEmails,
        additionalPhones = additionalPhones,
        personalAddresses = personalAddresses,
        hasCompanyProfile = hasCompanyProfile,
        companies = companies,
        isOnline = isOnline,
        isSubscribed = isSubscribed,
        isVerified = isVerified,
        notificationsEnabled = notificationsEnabled,
        isPublicProfile = isPublicProfile,
        isProfileComplete = isProfileComplete,
        rating = rating,
        favoriteProviderIds = favoriteProviderIds,
        latitude = latitude,
        longitude = longitude,
        createdAt = createdAt,
        isSynced = isSynced
    )
}
