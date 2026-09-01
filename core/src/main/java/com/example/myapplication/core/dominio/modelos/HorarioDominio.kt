package com.example.myapplication.core.dominio.modelos

import androidx.annotation.Keep

/**
 * --- MODELO DE DOMINIO HORARIO (SSOT 2026) ---
 * [LEY #9]: Estándar Maverick en Español.
 * Define la disponibilidad temporal para la lógica de negocio.
 */
@Keep
data class HorarioDominio(
    val lunes: List<RangoHorarioDominio> = emptyList(),
    val martes: List<RangoHorarioDominio> = emptyList(),
    val miercoles: List<RangoHorarioDominio> = emptyList(),
    val jueves: List<RangoHorarioDominio> = emptyList(),
    val viernes: List<RangoHorarioDominio> = emptyList(),
    val sabado: List<RangoHorarioDominio> = emptyList(),
    val domingo: List<RangoHorarioDominio> = emptyList(),
    val zonaHoraria: String = "America/Argentina/Buenos_Aires"
)

/**
 * --- MODELO DE DOMINIO RANGO HORARIO (SSOT 2026) ---
 */
@Keep
data class RangoHorarioDominio(
    val inicio: String = "08:00",
    val fin: String = "18:00",
    val estaHabilitado: Boolean = true
)
