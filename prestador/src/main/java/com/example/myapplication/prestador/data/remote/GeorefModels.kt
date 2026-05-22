package com.example.myapplication.prestador.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProvinciaResponse(
    @SerialName("provincias") val provincias: List<ProvinciaDto>,
    @SerialName("total") val total: Int
)

@Serializable
data class ProvinciaDto(
    @SerialName("id") val id: String,
    @SerialName("nombre") val nombre: String
)

@Serializable
data class LocalidadResponse(
    @SerialName("localidades") val localidades: List<LocalidadDto>,
    @SerialName("total") val total: Int
)

@Serializable
data class LocalidadDto(
    @SerialName("nombre") val nombre: String,
    @SerialName("codigo_postal") val codigoPostal: String? = null
)
