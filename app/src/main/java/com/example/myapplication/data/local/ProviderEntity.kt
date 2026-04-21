package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.model.AddressProvider
import com.example.myapplication.data.model.CompanyProvider
import com.example.myapplication.data.model.Provider

/**
 * --- ENTIDAD DE BASE DE DATOS PARA PROVEEDORES ---
 * [RESTRUCTURADO] Refleja el nuevo modelo de dominio con soporte para múltiples
 * correos, direcciones y la nueva jerarquía de empresas/sucursales.
 */
@Entity(tableName = "provider_profile")
data class ProviderEntity(
    @PrimaryKey val id: String,

    // --- SECCIÓN: DATOS DE CONTACTO ---
    val email: String,
    val alternateEmail: String? = null,
    val emails: List<String> = emptyList(), // Soporte para más de 1 correo electrónico
    val phoneNumber: String,
    val additionalPhones: List<String> = emptyList(),

    // --- SECCIÓN: DATOS DEL PRESTADOR ---
    val displayName: String,
    val name: String,
    val lastName: String,
    val matricula: String? = null,
    val titulo: String? = null,
    val cuilCuit: String? = null,

    // --- SECCIÓN: EMPRESAS Y DIRECCIONES (Crucial para la sincronización) ---
    val addresses: List<AddressProvider> = emptyList(), // Soporte para más de 1 dirección
    val address: AddressProvider? = null, // Dirección principal (compatibilidad)
    val companies: List<CompanyProvider> = emptyList(), 
    val hasCompanyProfile: Boolean = false, 

    // --- SECCIÓN: CARACTERÍSTICAS Y BOOLEANOS ---
    val doesService: Boolean = false, 
    val doesProduct: Boolean = false, 
    val works24h: Boolean = false, 
    val hasPhysicalLocation: Boolean = false, 
    val doesHomeVisits: Boolean = false, 
    val doesShipping: Boolean = false, 
    val acceptsAppointments: Boolean = false, 

    val isSubscribed: Boolean = false, 
    val isVerified: Boolean = false,  

    val isFavorite: Boolean = false, 
    val isOnline: Boolean = false,  

    // --- SECCIÓN: VALORACIÓN Y DETALLES ---
    val rating: Float = 0f,
    val workingHours: String = "", 
    val categories: List<String> = emptyList(),
    val description: String = "",

    // --- SECCIÓN: MULTIMEDIA Y METADATOS ---
    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,
    val galleryImages: List<String> = emptyList(),
    val favoriteProviderIds: List<String> = emptyList(),
    val createdAt: Long
) {
    /**
     * Convierte la entidad de la base de datos (ProviderEntity) en un objeto de dominio (Provider).
     */
    fun toDomain(): Provider {
        return Provider(
            uid = id,
            email = email,
            alternateEmail = alternateEmail,
            emails = emails,
            displayName = displayName,
            name = name,
            lastName = lastName,
            phoneNumber = phoneNumber,
            additionalPhones = additionalPhones,
            matricula = matricula,
            titulo = titulo,
            cuilCuit = cuilCuit,
            addresses = addresses,
            address = address,
            doesService = doesService,
            doesProduct = doesProduct,
            works24h = works24h,
            hasPhysicalLocation = hasPhysicalLocation,
            doesHomeVisits = doesHomeVisits,
            doesShipping = doesShipping,
            acceptsAppointments = acceptsAppointments,
            isSubscribed = isSubscribed,
            isVerified = isVerified,
            isFavorite = isFavorite,
            isOnline = isOnline,
            rating = rating,
            workingHours = workingHours,
            categories = categories,
            description = description,
            companies = companies,
            hasCompanyProfile = hasCompanyProfile,
            photoUrl = photoUrl,
            bannerImageUrl = bannerImageUrl,
            galleryImages = galleryImages,
            favoriteProviderIds = favoriteProviderIds,
            createdAt = createdAt
        )
    }

    companion object {
        /**
         * Crea una entidad a partir de un objeto de dominio.
         */
        fun fromDomain(provider: Provider): ProviderEntity {
            return ProviderEntity(
                id = provider.uid,
                email = provider.email,
                alternateEmail = provider.alternateEmail,
                emails = provider.emails,
                displayName = provider.displayName,
                name = provider.name,
                lastName = provider.lastName,
                phoneNumber = provider.phoneNumber,
                additionalPhones = provider.additionalPhones,
                matricula = provider.matricula,
                titulo = provider.titulo,
                cuilCuit = provider.cuilCuit,
                addresses = provider.addresses,
                address = provider.address,
                doesService = provider.doesService,
                doesProduct = provider.doesProduct,
                works24h = provider.works24h,
                hasPhysicalLocation = provider.hasPhysicalLocation,
                doesHomeVisits = provider.doesHomeVisits,
                doesShipping = provider.doesShipping,
                acceptsAppointments = provider.acceptsAppointments,
                isSubscribed = provider.isSubscribed,
                isVerified = provider.isVerified,
                isFavorite = provider.isFavorite,
                isOnline = provider.isOnline,
                rating = provider.rating,
                workingHours = provider.workingHours,
                categories = provider.categories,
                description = provider.description,
                companies = provider.companies,
                hasCompanyProfile = provider.hasCompanyProfile,
                photoUrl = provider.photoUrl,
                bannerImageUrl = provider.bannerImageUrl,
                galleryImages = provider.galleryImages,
                favoriteProviderIds = provider.favoriteProviderIds,
                createdAt = provider.createdAt
            )
        }
    }
}
