package com.example.myapplication.core.domain.model

import java.util.UUID

/**
 * --- MODELOS DE EMPRESA Y SUCURSALES (LADO CLIENTE) ---
 * Centraliza la estructura de las empresas que el cliente/usuario pueda poseer.
 */

/**
 * Representa la entidad legal o comercial de una empresa del cliente.
 */
data class CompanyClient(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // Nombre Comercial
    val razonSocial: String = "",
    val cuit: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val bannerImageUrl: String? = null,
    val photoUrl: String? = null,
    val branches: List<BranchClient> = emptyList()
)

/**
 * Representa una sucursal específica de la empresa.
 */
data class BranchClient(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "", // "Sucursal Centro", "Casa Central", etc.
    val isMainBranch: Boolean = false,
    val address: AddressClient = AddressClient(),
    val representatives: List<RepresentativeClient> = emptyList(), // Equipo de trabajo
    val galleryImages: List<String> = emptyList()
)

/**
 * Representa a una persona dentro del equipo de trabajo de una sucursal.
 */
data class RepresentativeClient(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String = "",
    val apellido: String = "",
    val cargo: String = "",
    val photoUrl: String? = null
)
