package com.example.myapplication.core.dominio.modelos

/**
 * --- PROMOCIÓN UI MODEL (ELITE v2026.FINAL) ---
 * [LEY #10]: Modelo desacoplado optimizado para visualización fluida en Compose.
 * Centraliza el formateo de tiempos y la resolución de imágenes.
 */
data class PromocionDominio(
    val id: String,
    val idPrestador: String,
    val titulo: String,
    val descripcion: String,
    val urlImagen: String?,
    val urlMiniaturaPrestador: String?,
    val nombrePrestador: String,
    val reputacion: Float,
    val estaVerificado: Boolean,
    
    // --- Lógica Visual (Pre-calculada) ---
    val tiempoRelativo: String, // Ej: "Hace 5 min" o "Expira en 2h"
    val etiquetaOferta: String?, // Ej: "30% OFF", "HOT SALE"
    val esHistoria: Boolean,
    val leGustaAlUsuario: Boolean,
    val conteoLikes: Int,
    val esNuevo: Boolean,
    
    // --- Publicidad (Opcional) ---
    val esPublicidad: Boolean = false,
    val nativeAd: Any? = null
)

































