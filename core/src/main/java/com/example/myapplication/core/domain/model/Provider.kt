package com.example.myapplication.core.domain.model

/**
 * --- MODELO DE DOMINIO: Provider (PERFIL DEL PRESTADOR) ---
 * Este objeto representa al profesional que ofrece servicios en la plataforma.
 * Es utilizado tanto por el cliente (para ver a quién contrata) como por el 
 * propio prestador (para gestionar su perfil).
 */
data class Provider(
    val uid: String,

    // --- DATOS DE CONTACTO ---
    val email: String,
    val alternateEmail: String? = null,
    val emails: List<String> = emptyList(),
    val phoneNumber: String,
    val additionalPhones: List<String> = emptyList(),

    // --- DATOS DEL PRESTADOR ---
    val displayName: String,
    val name: String,
    val lastName: String,
    val matricula: String? = null,
    val profesion: String? = null,
    val titulo: String? = null,
    val cuilCuit: String? = null,
    val addresses: List<AddressProvider> = emptyList(),
    val address: AddressProvider? = null, // Dirección principal

    // --- CONFIGURACIÓN DE SERVICIOS ---
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
    val isFavorite: Boolean = false,
    val isOnline: Boolean = false,

    // --- VALORACIÓN Y DESCRIPCIÓN ---
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
    val serviceType: String = "TECHNICAL",
    val createdAt: Long
) {
    val id: String get() = uid
    val profileImage: String? get() = photoUrl

    // --- ALIASES DE COMPATIBILIDAD UI ---
    val apellido: String get() = lastName
    val imageUrl: String? get() = photoUrl
    val verificado: Boolean get() = isVerified
    val vaDomicilio: Boolean get() = doesHomeVisits
    val suscripto: Boolean get() = isSubscribed
    val favorito: Boolean get() = isFavorite
    val tieneEmpresa: Boolean get() = hasCompanyProfile || companies.isNotEmpty()
    val nombreEmpresa: String? get() = companies.firstOrNull()?.name
    val direccionEmpresa: String? get() = companies.firstOrNull()?.branches?.firstOrNull()?.address?.fullString()
    val cuitEmpresa: String? get() = companies.firstOrNull()?.cuit
    val turnosEnLocal: Boolean get() = hasPhysicalLocation
    val localAddress: AddressProvider? get() = addresses.find { it.id == "local" } ?: address
    val direccionLocal: String? get() = localAddress?.fullString()
    val phone: String get() = phoneNumber
    val dniCuit: String? get() = cuilCuit
    val tieneMatricula: Boolean get() = !matricula.isNullOrBlank()
    val envios: Boolean get() = doesShipping
    val atencionUrgencias: Boolean get() = works24h
    val horarioLocal: String get() = workingHours
    val provincia: String? get() = address?.provincia
    val codigoPostal: String? get() = address?.codigoPostal
    val pais: String? get() = address?.pais
    val provinciaLocal: String? get() = localAddress?.provincia
    val codigoPostalLocal: String? get() = localAddress?.codigoPostal
}

/**
 * Extensión para convertir el modelo de dominio a la entidad de persistencia (SSOT).
 */
fun Provider.toEntity(): com.example.myapplication.core.data.local.entity.ProviderEntity {
    return com.example.myapplication.core.data.local.entity.ProviderEntity.fromDomain(this)
}
