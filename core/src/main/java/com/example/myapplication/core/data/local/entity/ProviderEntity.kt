package com.example.myapplication.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myapplication.core.domain.model.AddressUnico
import com.example.myapplication.core.domain.model.CompanyProvider
import com.example.myapplication.core.domain.model.Provider

/**
 * --- ENTIDAD DE PRESTADOR (ROOM) ---
 * [ELITE v5.1]: Usa AddressUnico y mantiene metadatos de búsqueda.
 */
@Entity(
    tableName = "provider_profile",
    indices = [Index(value = ["providerId"])] // [LEY #4] Optimización para Ley Pareja
)
data class ProviderEntity(
    @PrimaryKey val id: String, // ID único del punto de servicio (ej: uid_branchId)
    val providerId: String,    // UID real del prestador (para agrupamiento Ley Pareja)

    // --- MULTIMEDIA ---
    val photoUrl: String? = null,
    val profileThumbnail: String? = null, // [LEY #3]

    // --- DATOS DEL PROFESIONAL ---
    val displayName: String,
    val name: String,
    val lastName: String,
    val matricula: String? = null,
    val profesion: String? = null,
    val cuilCuit: String? = null,
    val description: String = "",

    // --- CONTACTO ---
    val email: String,
    val emails: List<String> = emptyList(),
    val phoneNumber: String,

    // --- UBICACIONES ---
    val addresses: List<AddressUnico> = emptyList(),
    val address: AddressUnico? = null,

    // --- ESTRUCTURA EMPRESARIAL ---
    val companies: List<CompanyProvider> = emptyList(),

    // --- CAPACIDADES (FILTROS) ---
    val doesService: Boolean = false,
    val doesProduct: Boolean = false,
    val works24h: Boolean = false,
    val hasPhysicalLocation: Boolean = false,
    val doesHomeVisits: Boolean = false,
    val doesShipping: Boolean = false,
    val acceptsAppointments: Boolean = false,
    val trabajaConOtros: Boolean = false,

    // --- ESTADOS ---
    val isSubscribed: Boolean = false,
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val isFavorite: Boolean = false,
    val priorizarEmpresa: Boolean = false,


    // --- VALORACIÓN Y CATEGORÍAS ---
    val rating: Float = 0f,
    val workingHours: String = "",
    val categories: List<String> = emptyList(),
    val serviceType: String? = null,
    val fullAddress: String? = null, // [ELITE] Dirección pre-formateada
    val geohash: String? = null, // [ELITE] Para proximidad

    // --- METADATOS ELITE ---
    val latitude: Double = 0.0, // Denormalizado para búsqueda rápida
    val longitude: Double = 0.0, // Denormalizado para búsqueda rápida
    val lastSyncTimestamp: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Mapea la entidad de Room al modelo de Dominio puro.
     */
    fun toDomain(): Provider {
        return Provider(
            uid = providerId, // [ELITE] Recuperamos el UID real del prestador
            photoUrl = photoUrl,
            profileThumbnail = profileThumbnail,
            displayName = displayName,
            name = name,
            lastName = lastName,
            matricula = matricula,
            profesion = profesion,
            cuilCuit = cuilCuit,
            description = description,
            email = email,
            emails = emails,
            phoneNumber = phoneNumber,
            addresses = addresses,
            address = address?.copy(latitude = latitude, longitude = longitude) ?: address,
            companies = companies,
            doesService = doesService,
            doesProduct = doesProduct,
            works24h = works24h,
            hasPhysicalLocation = hasPhysicalLocation,
            doesHomeVisits = doesHomeVisits,
            doesShipping = doesShipping,
            acceptsAppointments = acceptsAppointments,
            trabajaConOtros = trabajaConOtros,
            isSubscribed = isSubscribed,
            isVerified = isVerified,
            isOnline = isOnline,
            isFavorite = isFavorite,
            priorizarEmpresa = priorizarEmpresa,
            rating = rating,
            workingHours = workingHours,
            categories = categories,
            serviceType = serviceType,
            lastSyncTimestamp = lastSyncTimestamp,
            createdAt = createdAt
        )
    }

    companion object {
        /**
         * Crea una entidad de Room a partir de un modelo de Dominio.
         */
        fun fromDomain(p: Provider): ProviderEntity = ProviderEntity(
            id = p.uid,
            providerId = p.uid, // Por defecto coinciden
            photoUrl = p.photoUrl,
            profileThumbnail = p.profileThumbnail,
            displayName = p.displayName,
            name = p.name,
            lastName = p.lastName,
            matricula = p.matricula,
            profesion = p.profesion,
            cuilCuit = p.cuilCuit,
            description = p.description,
            email = p.email,
            emails = p.emails,
            phoneNumber = p.phoneNumber,
            addresses = p.addresses,
            address = p.address,
            companies = p.companies,
            doesService = p.doesService,
            doesProduct = p.doesProduct,
            works24h = p.works24h,
            hasPhysicalLocation = p.hasPhysicalLocation,
            doesHomeVisits = p.doesHomeVisits,
            doesShipping = p.doesShipping,
            acceptsAppointments = p.acceptsAppointments,
            trabajaConOtros = p.trabajaConOtros,
            isSubscribed = p.isSubscribed,
            isVerified = p.isVerified,
            isOnline = p.isOnline,
            isFavorite = p.isFavorite,
            priorizarEmpresa = p.priorizarEmpresa,
            rating = p.rating,
            workingHours = p.workingHours,
            categories = p.categories,
            serviceType = p.serviceType,
            fullAddress = p.address?.fullString(),
            latitude = p.address?.latitude ?: 0.0,
            longitude = p.address?.longitude ?: 0.0,
            lastSyncTimestamp = p.lastSyncTimestamp,
            createdAt = p.createdAt
        )
    }
}
