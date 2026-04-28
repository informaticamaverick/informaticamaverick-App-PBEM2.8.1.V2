package com.example.myapplication.data.model

/**
 * --- MODELO DE DOMINIO PARA PROVEEDORES ---
 * [RESTRUCTURADO] Según los nuevos requerimientos de base de datos.
 * Representa el perfil del prestador con sus datos personales, características y empresas.
 */
data class Provider(
    val uid: String,

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
    val addresses: List<AddressProvider> = emptyList(), // Soporte para más de 1 dirección
    val address: AddressProvider? = null, // Dirección principal (compatibilidad)

    // --- SECCIÓN: CARACTERÍSTICAS Y BOOLEANOS ---
    val doesService: Boolean = false, // Realiza Servicio??
    val doesProduct: Boolean = false, // Vende Producto??
    val works24h: Boolean = false, // Trabaja 24hs??
    val hasPhysicalLocation: Boolean = false, // Tiene Local??
    val doesHomeVisits: Boolean = false, // Hace Visitas Tecnicas??
    val doesShipping: Boolean = false, // Hace Envios??
    val acceptsAppointments: Boolean = false, // Acepta Turnos??

    val isSubscribed: Boolean = false, // Suscrito a la plataforma (pago mensual)
    val isVerified: Boolean = false,  // Verificado legalmente

    val isFavorite: Boolean = false, // Favorito para el usuario
    val isOnline: Boolean = false,  // Estado de conexión

    // --- SECCIÓN: VALORACIÓN Y DETALLES ---
    val rating: Float = 0f, // Ranking de valoración del cliente
    val workingHours: String = "", // Horario de atención del prestador
    val categories: List<String> = emptyList(),
    val description: String = "",

    // --- SECCIÓN: EMPRESAS ASOCIADAS ---
    val companies: List<CompanyProvider> = emptyList(), // Lista de empresas asociadas
    val hasCompanyProfile: Boolean = false, // ¿Tiene perfil de empresa?
    val priorizarEmpresa: Boolean = false, // ¿Mostrar perfil empresa primero?

    // --- SECCIÓN: MULTIMEDIA Y METADATOS ---
    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,
    val galleryImages: List<String> = emptyList(),
    val favoriteProviderIds: List<String> = emptyList(),
    val createdAt: Long
) {
    // --- PROPIEDADES DE COMPATIBILIDAD (Bridge para la UI) ---
    val id: String get() = uid
    val profileImage: String? get() = photoUrl
}
