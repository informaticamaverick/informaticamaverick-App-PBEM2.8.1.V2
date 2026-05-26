package com.example.myapplication.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.core.domain.model.AddressProvider
import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.core.domain.model.Provider

/**
 * --- ENTIDAD DE PRESTADOR (ROOM) ---
 * Almacena el perfil completo de un prestador en la base de datos local.
 * Esto permite búsquedas rápidas ("Costo Cero") sin consultar siempre a Firebase.
 */
@Entity(tableName = "provider_profile")
data class ProviderEntity(
    @PrimaryKey val id: String,

    // --- DATOS DE CONTACTO ---
    val email: String,
    val alternateEmail: String? = null,
    val emails: List<String> = emptyList(),
    val phoneNumber: String,
    val additionalPhones: List<String> = emptyList(),

    // --- DATOS DEL PROFESIONAL ---
    val displayName: String,
    val name: String,
    val lastName: String,
    val matricula: String? = null,
    val titulo: String? = null,
    val cuilCuit: String? = null,
    val profesion: String? = null, // [SSOT: Agregado para unificación prestador]
    val addresses: List<AddressProvider> = emptyList(),
    val address: AddressProvider? = null,

    // --- CAPACIDADES ---
    val doesService: Boolean = false,
    val doesProduct: Boolean = false,
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val trabajaConOtros: Boolean = false, // [SSOT: Agregado para unificación prestador]

    // --- ESTADOS ---
    val isSubscribed: Boolean = false,
    val isVerified: Boolean = false,
    val isFavorite: Boolean = false,
    val isOnline: Boolean = false,

    // --- VALORACIÓN Y CATEGORÍAS ---
    val rating: Float = 0f,
    val workingHours: String = "",
    val categories: List<String> = emptyList(),
    val description: String = "",

    // --- PERFIL EMPRESARIAL ---
    val companies: List<CompanyProvider> = emptyList(),
    val hasCompanyProfile: Boolean = false,
    val priorizarEmpresa: Boolean = false,

    // --- MULTIMEDIA ---
    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,
    val galleryImages: List<String> = emptyList(),
    val favoriteProviderIds: List<String> = emptyList(),
    val serviceType: String = "PROFESSIONAL", // [SSOT: Agregado para unificación prestador]
    val createdAt: Long
) {
    /**
     * Mapea la entidad de Room al modelo de Dominio puro.
     */
    fun toDomain(): Provider = Provider(
        uid = id,
        email = email,
        alternateEmail = alternateEmail,
        emails = emails,
        phoneNumber = phoneNumber,
        additionalPhones = additionalPhones,
        displayName = displayName,
        name = name,
        lastName = lastName,
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
        priorizarEmpresa = priorizarEmpresa,
        photoUrl = photoUrl,
        bannerImageUrl = bannerImageUrl,
        galleryImages = galleryImages,
        favoriteProviderIds = favoriteProviderIds,
        createdAt = createdAt
    )

    companion object {
        /**
         * Crea una entidad de Room a partir de un modelo de Dominio.
         */
        fun fromDomain(p: Provider): ProviderEntity = ProviderEntity(
            id = p.uid,
            email = p.email,
            alternateEmail = p.alternateEmail,
            emails = p.emails,
            phoneNumber = p.phoneNumber,
            additionalPhones = p.additionalPhones,
            displayName = p.displayName,
            name = p.name,
            lastName = p.lastName,
            matricula = p.matricula,
            titulo = p.titulo,
            cuilCuit = p.cuilCuit,
            addresses = p.addresses,
            address = p.address,
            doesService = p.doesService,
            doesProduct = p.doesProduct,
            works24h = p.works24h,
            hasPhysicalLocation = p.hasPhysicalLocation,
            doesHomeVisits = p.doesHomeVisits,
            doesShipping = p.doesShipping,
            acceptsAppointments = p.acceptsAppointments,
            isSubscribed = p.isSubscribed,
            isVerified = p.isVerified,
            isFavorite = p.isFavorite,
            isOnline = p.isOnline,
            rating = p.rating,
            workingHours = p.workingHours,
            categories = p.categories,
            description = p.description,
            companies = p.companies,
            hasCompanyProfile = p.hasCompanyProfile,
            priorizarEmpresa = p.priorizarEmpresa,
            photoUrl = p.photoUrl,
            bannerImageUrl = p.bannerImageUrl,
            galleryImages = p.galleryImages,
            favoriteProviderIds = p.favoriteProviderIds,
            createdAt = p.createdAt
        )
    }
}
