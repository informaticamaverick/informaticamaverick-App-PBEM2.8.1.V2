package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable

/**
 * --- MODELO DE DOMINIO PRESTADOR (SSOT 2026) ---
 * [LEY #9]: Estándar Maverick en Español.
 * Representa a un profesional para su lógica de negocio.
 */
@Keep
@Immutable
data class PrestadorDominio(
    val id: String = "",
    val idPropietario: String = "",
    val idEmpresa: String? = null,
    val tipo: TipoPrestador = TipoPrestador.INDIVIDUAL,
    
    // --- IDENTIDAD VISUAL Y SOBERANÍA ---
    val nombre: String = "",
    val apellido: String = "",
    val titulo: String = "",
    val subtitulo: String? = null,
    val biografia: String? = null,
    val urlFoto: Any? = null,      // Soporta URL (String) o Base64 (ByteArray)
    val urlMiniatura: Any? = null,  // Soporta URL (String) o Base64 (ByteArray)
    val correo: String = "",
    val esGoogle: Boolean = false,
    val numeroTelefono: String = "",
    val cuitCuil: String? = null,
    val matricula: String? = null,
    
    // --- MÉTRICAS ELITE (SSOT) ---
    val reputacion: Float = 0f,
    val totalReseñas: Int = 0,
    val trabajosRealizados: Int = 0,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val nivelElite: Int = 0,
    
    // --- STATUS Y CONTROL ---
    val estaVerificado: Boolean = false,
    val estaOnline: Boolean = false,
    val estaSuscrito: Boolean = false,
    val esPerfilComercial: Boolean = true, // [LEY #10]: Interruptor de modo Negocio vs Modo Identidad
    val esCargaCompleta: Boolean = false, // [ELITE]: Flag de sincronización profunda finalizada
    
    // --- CAPACIDADES OPERATIVAS ---
    val brindaServicio: Boolean = false,
    val brindaProducto: Boolean = false,
    val atiende24h: Boolean = false,
    val visitaADomicilio: Boolean = false,
    val realizaEnvios: Boolean = false,
    val brindaTurnos: Boolean = false,
    val tieneLocalFisico: Boolean = false,
    
    // --- UBICACIÓN (Ley #4) ---
    val direccionVisible: String? = null,
    val direcciones: List<DireccionDominio> = emptyList(),
    val nombreSucursal: String? = null,
    val codigoPostal: String? = null,
    val distanciaKm: Double? = null,
    val latitud: Double? = null, // 🔥 [NEW]: Para visualización en Radar/Mapas
    val longitud: Double? = null, // 🔥 [NEW]
    
    // --- EXTRAS UI ---
    val insignias: List<PerfilPrestadorInsignia> = emptyList(),
    val textoEstado: String? = null,
    val idCategorias: List<String> = emptyList(),
    val filtrosBusqueda: List<String> = emptyList(),
    val horario: HorarioDominio? = null, // [ELITE]: Objeto de disponibilidad para renderizado de matriz
    val reseñas: List<ReseñaDominio> = emptyList() // [ELITE]: Lista de opiniones certificadas
)

