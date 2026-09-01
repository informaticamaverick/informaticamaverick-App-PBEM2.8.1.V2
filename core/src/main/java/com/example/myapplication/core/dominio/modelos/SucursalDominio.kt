package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO SUCURSAL (SSOT 2026) ---
 * [LEY #9]: Estándar Maverick en Español.
 * Representa un punto operativo puro para lógica de negocio.
 */
@Keep
data class SucursalDominio(
    val id: String = "",
    val idEmpresaPadre: String = "",
    val idPropietario: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val numeroTelefono: String = "",
    val reputacion: Float = 0f,
    val totalReseñas: Int = 0,
    val trabajosRealizados: Int = 0,
    val likes: Int = 0,
    val dislikes: Int = 0,
    val estaEnLinea: Boolean = false,
    val brindaServicio: Boolean = false,
    val brindaProducto: Boolean = false,
    val atiende24Horas: Boolean = false,
    val visitaADomicilio: Boolean = false,
    val realizaEnvios: Boolean = false,
    val brindaTurnos: Boolean = false,
    val usaAgendaRecursos: Boolean = false,
    val capacidadSimultanea: Int = 1
)
