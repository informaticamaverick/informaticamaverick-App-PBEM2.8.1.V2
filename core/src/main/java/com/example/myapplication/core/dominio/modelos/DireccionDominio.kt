package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep
import java.util.UUID

/**
 * --- MODELO DE DOMINIO DIRECCIÓN (SSOT 2026) ---
 * [LEY #9]: Estándar Maverick en Español.
 * Representa una dirección física unificada para todo el ecosistema.
 */
@Keep
data class DireccionDominio(
    val id: String = UUID.randomUUID().toString(),
    val calle: String = "",
    val numero: String = "",
    val piso: String = "",
    val departamento: String = "",
    val localidad: String = "",
    val provincia: String = "",
    val pais: String = "Argentina",
    val codigoPostal: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val geohash: String = "",
    val etiqueta: String = "", 
    val estaVerificadaGps: Boolean = false, 
    val precisionGps: Float = 0f,          
    val tieneLocalFisico: Boolean = false,
    val tipo: TipoDireccion = TipoDireccion.PERFIL_USUARIO,

    // --- METADATOS DE CONTEXTO ---
    val idPropietario: String? = null,
    val idSucursal: String? = null,
    val idReferencia: String? = null,
    val nombreSucursal: String? = null, // Solo para transporte en UI, no se guarda en Entity
    val esEmpresa: Boolean = false,      // Solo para transporte en UI, no se guarda en Entity

    // --- SINCRO ---
    val ultimaSincronizacion: Long = 0L
) {
    /**
     * Obtiene calle y número combinados.
     */
    val calleYNumero: String
        get() = listOf(calle, numero).filter { it.isNotBlank() }.joinToString(" ")

    /**
     * Devuelve la dirección formateada como una sola línea de texto.
     */
    fun aTextoCompleto(): String {
        val pisoDepto = listOf(piso, departamento).filter { it.isNotBlank() }.joinToString(" ")
        return listOf(calleYNumero, pisoDepto, localidad, provincia, pais)
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }

    /**
     * Devuelve la dirección formateada de forma compacta (Calle, Altura, Piso/Depto).
     */
    fun aTextoCorto(): String {
        val pisoDepto = if (piso.isNotBlank() || departamento.isNotBlank()) {
            " (" + listOf(if (piso.isNotBlank()) "Piso $piso" else "", departamento).filter { it.isNotBlank() }.joinToString(" ") + ")"
        } else ""
        return "$calleYNumero$pisoDepto"
    }
}

