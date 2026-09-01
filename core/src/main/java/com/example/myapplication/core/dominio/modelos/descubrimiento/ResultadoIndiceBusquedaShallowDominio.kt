package com.example.myapplication.core.dominio.modelos.descubrimiento

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO: RESULTADO DEL ÍNDICE DE BÚSQUEDA (v2026.ELITE) ---
 * [PROPÓSITO]: Representar un resultado shallow (ligero) descargado de Firebase.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Keep
data class ResultadoIndiceBusquedaShallowDominio(
    val id: String = "",
    val idPropietario: String = "",
    val idPadre: String? = null,    // ID de la Empresa (si aplica)
    val tipoIdentidad: String = "", // PRESTADOR, SUCURSAL
    
    // --- IDENTIDAD VISUAL ---
    val nombreVisible: String = "",
    val nombreEmpresa: String? = null,
    val urlFoto: String? = null,
    val miniaturaBase64: String? = null,
    
    // --- MÉTRICAS Y STATUS ---
    val reputacion: Float = 0f,
    val totalReseñas: Int = 0,
    val trabajosRealizados: Int = 0,
    val estaSuscrito: Boolean = false,
    val estaVerificado: Boolean = false,
    val estaEnLinea: Boolean = false,
    
    // --- UBICACIÓN ---
    val calle: String = "",
    val numero: String = "",
    val codigoPostal: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val geohash: String = "",
    
    // --- FILTRADO (Ley #14) ---
    val idCategorias: List<String> = emptyList(),
    val filtrosBusqueda: List<String> = emptyList(),
    
    // --- CAPACIDADES ---
    val brindaServicio: Boolean = false,
    val brindaProducto: Boolean = false,
    val brindaTurnos: Boolean = false,
    val atiende24h: Boolean = false,
    val realizaEnvios: Boolean = false,
    val visitaADomicilio: Boolean = false,
    val tieneLocalFisico: Boolean = false
)
