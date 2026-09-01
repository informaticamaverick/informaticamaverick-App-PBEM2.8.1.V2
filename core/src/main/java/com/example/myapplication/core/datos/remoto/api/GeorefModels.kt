package com.example.myapplication.core.datos.remoto.api

import com.google.gson.annotations.SerializedName

/**
 * --- MODELOS DE RESPUESTA GEOREF (ARGENTINA) ---
 * [ELITE]: Utilizados para la normalización oficial de direcciones.
 * [v2026]: Implementados con GSON para máxima compatibilidad.
 */
data class ProvinciaResponse(
    @SerializedName("provincias") val provincias: List<ProvinciaDto>,
    @SerializedName("total") val total: Int
)

data class ProvinciaDto(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String
)

data class LocalidadResponse(
    @SerializedName("localidades") val localidades: List<LocalidadDto>,
    @SerializedName("total") val total: Int
)

data class LocalidadDto(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("codigo_postal") val codigoPostal: String? = null
)

































