package com.example.myapplication.prestador.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.prestador.data.model.AddressProvider
import com.example.myapplication.prestador.data.model.CompanyProvider
import com.example.myapplication.prestador.data.model.Provider

/**
 * --- ENTIDAD DE BASE DE DATOS PARA PROVEEDORES (REPLICADO) ---
 * [MIGRACIÓN SSOT]: Esta entidad será reemplazada por com.example.myapplication.core.data.local.entity.ProviderEntity
 * del módulo `:core`. Favor no editar este archivo, usar la entidad unificada.
 *
 * Refleja el modelo de dominio con soporte para múltiples
 * correos, direcciones y la jerarquía de empresas/sucursales.
 */
@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,

    // --- SECCIÓN: DATOS DE CONTACTO ---
    val email: String,
    val alternateEmail: String? = null,
    val emails: List<String> = emptyList(), 
    val phoneNumber: String,
    val additionalPhones: List<String> = emptyList(),

    // --- SECCIÓN: DATOS DEL PRESTADOR ---
    val displayName: String,
    val name: String,
    val lastName: String,
    val matricula: String? = null,
    val titulo: String? = null,
    val cuilCuit: String? = null,
    val profesion: String? = null,

    // --- SECCIÓN: EMPRESAS Y DIRECCIONES ---
    val addresses: List<AddressProvider> = emptyList(), 
    val address: AddressProvider? = null, 
    val companies: List<CompanyProvider> = emptyList(), 
    val hasCompanyProfile: Boolean = false,
    val priorizarEmpresa: Boolean = false,

    // --- SECCIÓN: CARACTERÍSTICAS Y BOOLEANOS ---
    val doesService: Boolean = false, 
    val doesProduct: Boolean = false, 
    val works24h: Boolean = false, 
    val hasPhysicalLocation: Boolean = false, 
    val doesHomeVisits: Boolean = false, 
    val doesShipping: Boolean = false, 
    val acceptsAppointments: Boolean = false,
    val trabajaConOtros: Boolean = false,

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
    val serviceType: String = "PROFESSIONAL",
    val createdAt: Long
) {
    // --- ALIASES PARA COMPATIBILIDAD CON UI ---
    val apellido: String get() = lastName
    val imageUrl: String? get() = photoUrl
    val verificado: Boolean get() = isVerified
    val vaDomicilio: Boolean get() = doesHomeVisits
    val suscripto: Boolean get() = isSubscribed
    val favorito: Boolean get() = isFavorite
    val tieneEmpresa: Boolean get() = hasCompanyProfile
    val nombreEmpresa: String? get() = companies.firstOrNull()?.name
    val direccionEmpresa: String? get() = companies.firstOrNull()?.branches?.firstOrNull()?.address?.fullString()
    val cuitEmpresa: String? get() = companies.firstOrNull()?.cuit
    val turnosEnLocal: Boolean get() = hasPhysicalLocation
    val direccionLocal: String? get() = address?.fullString()
    val phone: String get() = phoneNumber
    val dniCuit: String? get() = cuilCuit
    val tieneMatricula: Boolean get() = !matricula.isNullOrBlank()
    val envios: Boolean get() = doesShipping
    val atencionUrgencias: Boolean get() = works24h
    val horarioLocal: String get() = workingHours
    val provincia: String? get() = address?.provincia
    val codigoPostal: String? get() = address?.codigoPostal
    val pais: String? get() = address?.pais
    val provinciaLocal: String? get() = address?.provincia
    val codigoPostalLocal: String? get() = address?.codigoPostal

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
            profesion = profesion,
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
            trabajaConOtros = trabajaConOtros,
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
            serviceType = serviceType,
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
                profesion = provider.profesion,
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
                trabajaConOtros = provider.trabajaConOtros,
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
                priorizarEmpresa = provider.priorizarEmpresa,
                photoUrl = provider.photoUrl,
                bannerImageUrl = provider.bannerImageUrl,
                galleryImages = provider.galleryImages,
                favoriteProviderIds = provider.favoriteProviderIds,
                serviceType = provider.serviceType,
                createdAt = provider.createdAt
            )
        }
    }
}
