package com.example.myapplication.core.dominio.modelos.shallow

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO: PRESTADOR SHALLOW (v2026.ELITE) ---
 * [PROPÓSITO]: Identidad mínima de un profesional para el índice de búsqueda.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Keep
data class PrestadorShallowDominio(
    val id: String = "",
    val idPropietario: String = "",
    val nombreVisible: String = "",
    val urlFoto: Any? = null,      // [ELITE]: Soporta URL o ByteArray
    val miniaturaBase64: Any? = null, // [ELITE]: Soporta URL o ByteArray
    val reputacion: Float = 0f,
    val trabajosRealizados: Int = 0,
    val estaVerificado: Boolean = false,
    val estaEnLinea: Boolean = false,
    val estaSuscrito: Boolean = false,
    
    // --- CAPACIDADES RÁPIDAS ---
    val brindaServicio: Boolean = false,
    val brindaProducto: Boolean = false,
    val brindaTurnos: Boolean = false,
    val atiende24Horas: Boolean = false,
    val visitaADomicilio: Boolean = false,
    val realizaEnvios: Boolean = false,
    val tieneLocalFisico: Boolean = false,
    
    // --- GEOLOCALIZACIÓN ---
    val calle: String = "",
    val numero: String = "",
    val codigoPostal: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    
    // --- FILTRADO (Tags) ---
    val idCategorias: List<String> = emptyList(),
    val filtrosBusqueda: List<String> = emptyList()
)
