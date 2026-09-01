package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO PRODUCTO (SSOT 2026) ---
 * [LEY #9]: Estándar Maverick en Español.
 * Representa un ítem del catálogo o línea de presupuesto pura.
 */
@Keep
data class ProductoDominio(
    val id: String? = null,
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val precioCosto: Double = 0.0,
    val impuestoDefault: Double = 0.0,
    val descuentoDefault: Double = 0.0,
    val moneda: String = "ARS",
    val stockActual: Int = 0,
    val stockMinimo: Int = 0,
    val urlImagen: String? = null,
    val miniaturaBase64: String? = null,
    val esServicio: Boolean = false,
    val idCategoria: String = "GENERAL",
    val nombreCategoria: String? = null,
    val iconoCategoria: String? = null,
    
    // --- Metadatos de Transacción (Opcionales) ---
    val cantidadSeleccionada: Int = 1,
    val totalLinea: Double = 0.0
)
