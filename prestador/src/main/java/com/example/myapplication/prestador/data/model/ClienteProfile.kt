package com.example.myapplication.prestador.data.model

data class ClienteProfile(
    val clientId: String = "",
    val name: String = "",
    val lastName: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val isSubscribed: Boolean = false,
    val isPublicProfile: Boolean = false,
    val rating: Float = 0f,
    val galleryImages: List<String> = emptyList(),
    val personalAddresses: List<ClienteDireccion> = emptyList(),
    val hasCompanyProfile: Boolean = false,
    val companies: List<ClienteEmpresa> = emptyList(),
    val createdAt: Long = 0L
) {
    val fullName: String
        get() = if (name.isNotBlank() || lastName.isNotBlank()) "$name $lastName".trim()
                else displayName
}

data class ClienteDireccion(
    val label: String = "",
    val calle: String = "",
    val numero: String = "",
    val localidad: String = "",
    val provincia: String = "",
    val pais: String = "",
    val codigoPostal: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    fun fullString(): String =
        listOf("$calle $numero".trim(), localidad, provincia, pais)
            .filter { it.isNotBlank() }
            .joinToString(", ")
}

data class ClienteEmpresa(
    val id: String = "",
    val name: String = "",
    val razonSocial: String = "",
    val cuit: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val bannerImageUrl: String? = null,
    val branches: List<ClienteSucursal> = emptyList()
)

data class ClienteSucursal(
    val id: String = "",
    val name: String = "",
    val isMainBranch: Boolean = false,
    val galleryImages: List<String> = emptyList(),
    val address: ClienteDireccion = ClienteDireccion()
)