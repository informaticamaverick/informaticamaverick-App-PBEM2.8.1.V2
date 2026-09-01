package com.example.myapplication.core.dominio.modelos.shallow

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO: SUCURSAL SHALLOW (v2026.ELITE) ---
 * [PROPÓSITO]: Identidad mínima de un punto de venta para el índice de búsqueda.
 * [LEY #9]: Estándar Maverick en Español.
 */
@Keep
data class SucursalShallowDominio(
    val id: String = "",
    val idPropietario: String = "",
    val idEmpresaPadre: String = "",
    val nombreSucursal: String = "",
    val nombreEmpresa: String = "",
    val urlFoto: Any? = null,
    val miniaturaBase64: Any? = null,
    val reputacion: Float = 0f,
    val estaEnLinea: Boolean = false,
    val estaSuscrito: Boolean = false,
    
    // --- CAPACIDADES RÁPIDAS ---
    val brindaServicio: Boolean = false,
    val brindaProducto: Boolean = false,
    val atiende24Horas: Boolean = false,
    val visitaADomicilio: Boolean = false,
    val realizaEnvios: Boolean = false,
    
    // --- GEOLOCALIZACIÓN ---
    val calle: String = "",
    val numero: String = "",
    val codigoPostal: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    
    // --- FILTRADO (Tags heredados de empresa) ---
    val idCategorias: List<String> = emptyList(),
    val filtrosBusqueda: List<String> = emptyList()
)
