package com.example.myapplication.core.domain.model

import java.util.UUID

/**
 * --- MODELOS DE EMPRESA Y SUCURSALES (LADO CLIENTE) ---
 * Estructura para las empresas que el cliente/usuario pueda poseer.
 * [ELITE v5.1]: Sin categorías ni flags de servicio.
 */

data class CompanyClient(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val razonSocial: String = "",
    val cuit: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String? = null,
    val thumbnailBase64: String? = null, // [LEY #3]

    val branches: List<BranchClient> = emptyList()
)

data class BranchClient(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "", // [ELITE v5.2]
    val workingHours: String = "", // [ELITE v5.2]
    val isMainBranch: Boolean = false,
    val address: AddressUnico = AddressUnico(),
    val representatives: List<RepresentativeClient> = emptyList()
)

data class RepresentativeClient(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val apellido: String = "",
    val cargo: String = "",
    val photoUrl: String? = null,
    val thumbnailBase64: String? = null // [LEY #3]
)
