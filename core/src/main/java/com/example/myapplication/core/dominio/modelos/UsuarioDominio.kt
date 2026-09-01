package com.example.myapplication.core.dominio.modelos

/**
 * --- MODELO DE DOMINIO USUARIO (SSOT 2026) ---
 * [LEY #9]: Estándar Maverick en Español.
 * Representa a un cliente para su lógica de negocio.
 */
data class UsuarioDominio(
    val id: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val nombreVisible: String = "",
    val urlFoto: Any? = null,      // Soporta URL (String) o Base64 (ByteArray)
    val urlMiniatura: Any? = null,  // Soporta URL (String) o Base64 (ByteArray)
    val estaOnline: Boolean = false,
    val correo: String = "",
    val telefono: String = "",
    val cuitCuil: String = "",
    val biografia: String = "",
    
    // --- MÉTRICAS DE CONFIANZA (Ley de Confianza) ---
    val reputacion: Float = 0f,
    val totalReseñas: Int = 0,
    val trabajosContratados: Int = 0,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val estaSuscrito: Boolean = false,
    
    // --- CONTROL DE PROFUNDIDAD ---
    val esCargaCompleta: Boolean = false
)

































